package com.zhz.bytedance.development_assistant_zhz;

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.EditorKind;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.fileEditor.impl.FileEditorManagerImpl;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.testFramework.LightVirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static java.lang.String.format;

public final class EditorUtil {

  public static Editor createEditor(@NotNull Project project, String fileExtension, String code) {
    var timestamp = DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(LocalDateTime.now());
    var fileName = "temp_" + timestamp + fileExtension;
    var lightVirtualFile = new LightVirtualFile(
        format("%s/%s", PathManager.getTempPath(), fileName),
        code);
    var existingDocument = FileDocumentManager.getInstance().getDocument(lightVirtualFile);
    var document = existingDocument != null
        ? existingDocument
        : EditorFactory.getInstance().createDocument(code);

    disableHighlighting(project, document);

    return EditorFactory.getInstance().createEditor(
        document,
        project,
        lightVirtualFile,
        true,
        EditorKind.MAIN_EDITOR);
  }

  public static void updateEditorDocument(Editor editor, String content) {
    var document = editor.getDocument();
    var application = ApplicationManager.getApplication();
    Runnable updateDocumentRunnable = () -> application.runWriteAction(() ->
        WriteCommandAction.runWriteCommandAction(editor.getProject(), () -> {
          document.replaceString(0, document.getTextLength(), content);
          editor.getComponent().repaint();
          editor.getComponent().revalidate();
        }));

    if (application.isUnitTestMode()) {
      application.invokeAndWait(updateDocumentRunnable);
    } else {
      application.invokeLater(updateDocumentRunnable);
    }
  }

  public static boolean hasSelection(@Nullable Editor editor) {
    return editor != null && editor.getSelectionModel().hasSelection();
  }

  public static @Nullable Editor getSelectedEditor(@NotNull Project project) {
    FileEditorManager editorManager = FileEditorManager.getInstance(project);
    return editorManager != null ? editorManager.getSelectedTextEditor() : null;
  }

  public static @Nullable String getSelectedEditorSelectedText(@NotNull Project project) {
    var selectedEditor = EditorUtil.getSelectedEditor(project);
    if (selectedEditor != null) {
      return selectedEditor.getSelectionModel().getSelectedText();
    }
    return null;
  }

  public static boolean isSelectedEditor(Editor editor) {
    Project project = editor.getProject();
    if (project != null && !project.isDisposed()) {
      FileEditorManager editorManager = FileEditorManager.getInstance(project);
      if (editorManager == null) {
        return false;
      }
      if (editorManager instanceof FileEditorManagerImpl) {
        Editor current = ((FileEditorManagerImpl) editorManager).getSelectedTextEditor(true);
        return current != null && current.equals(editor);
      }
      FileEditor current = editorManager.getSelectedEditor();
      return current instanceof TextEditor && editor.equals(((TextEditor) current).getEditor());
    }
    return false;
  }

  public static boolean isMainEditorTextSelected(@NotNull Project project) {
    return hasSelection(getSelectedEditor(project));
  }

  public static void replaceMainEditorSelection(@NotNull Project project, @NotNull String text) {
    var application = ApplicationManager.getApplication();
    application.invokeLater(() ->
        application.runWriteAction(() -> WriteCommandAction.runWriteCommandAction(project, () -> {
          var editor = getSelectedEditor(project);
          if (editor != null) {
            var selectionModel = editor.getSelectionModel();
            int startOffset = selectionModel.getSelectionStart();
            int endOffset = selectionModel.getSelectionEnd();
            var document = editor.getDocument();
            document.replaceString(startOffset, endOffset, text);
            editor.getContentComponent().requestFocus();
            selectionModel.removeSelection();
          }
        })));
  }

  public static void reformatDocument(
      @NotNull Project project,
      @NotNull Document document,
      int startOffset,
      int endOffset) {
    var psiDocumentManager = PsiDocumentManager.getInstance(project);
    psiDocumentManager.commitDocument(document);
    var psiFile = psiDocumentManager.getPsiFile(document);
    if (psiFile != null) {
      CodeStyleManager.getInstance(project)
          .reformatText(psiFile, startOffset, endOffset);
    }
  }

  public static void disableHighlighting(@NotNull Project project, Document document) {
    var psiFile = PsiDocumentManager.getInstance(project).getPsiFile(document);
    if (psiFile != null) {
      DaemonCodeAnalyzer.getInstance(project).setHighlightingEnabled(psiFile, false);
    }
  }
}
