package code_assistant.statusbar.copilot

import code_assistant.tool.Bundle
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.impl.status.widget.StatusBarEditorBasedWidgetFactory

class CopilotStatusBarWidgetFactory : StatusBarEditorBasedWidgetFactory() {

    override fun getId(): String {
        return Bundle.message("copilot.id", "");
    }

    override fun getDisplayName(): String {
        return Bundle.message("copilot.name", "");
    }

    override fun createWidget(project: Project): StatusBarWidget {
        return CopilotStatusBarWidget(project);
    }

    override fun disposeWidget(widget: StatusBarWidget) {
        Disposer.dispose(widget);
    }

}