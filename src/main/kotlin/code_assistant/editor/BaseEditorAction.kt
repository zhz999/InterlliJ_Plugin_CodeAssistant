package code_assistant.editor

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NlsActions
import javax.swing.Icon

/**
 * 注册 Editor 右键 Action
 */
internal abstract class BaseEditorAction @JvmOverloads constructor(
    text: @NlsActions.ActionText String?,
    description: @NlsActions.ActionDescription String?,
    icon: Icon? = null
) : AnAction(text, description, icon) {
    init {
        EditorActionsUtil.registerOrReplaceAction(this)
    }

    protected abstract fun actionPerformed(project: Project?, editor: Editor?, selectedText: String?)

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project
        val editor = event.getData(PlatformDataKeys.EDITOR)
        if (editor != null && project != null) {
            actionPerformed(project, editor, editor.selectionModel.selectedText)
        }
    }
    override fun update(event: AnActionEvent) {
        val project = event.project
        val editor = event.getData(PlatformDataKeys.EDITOR)
        var menuAllowed = false
        if (editor != null && project != null) {
            menuAllowed = editor.selectionModel.selectedText != null
        }
        event.presentation.setEnabled(menuAllowed)
    }
}
