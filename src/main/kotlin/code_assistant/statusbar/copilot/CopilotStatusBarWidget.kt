package code_assistant.statusbar.copilot

import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.ui.popup.ListPopup
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.impl.status.EditorBasedStatusBarPopup
import code_assistant.common.Icons
import code_assistant.tool.Bundle
import kotlinx.coroutines.Runnable

class CopilotStatusBarWidget(project: Project) : EditorBasedStatusBarPopup(project, false) {

    object WidgetStates {
        @JvmStatic
        @Volatile
        var icon = Icons.Disabled
    }

    override fun ID(): String {
        return Bundle.message("copilot.id", "");
    }

    override fun getWidgetState(file: VirtualFile?): WidgetState {
        val state = WidgetState(Bundle.message("copilot.name", ""), " " + Bundle.message("copilot.name", ""), true);
        state.icon = WidgetStates.icon
        return state;
    }

    override fun createPopup(context: DataContext): ListPopup {
        val actionGroup = ActionManager.getInstance().getAction("StatusBar.CopilotStatusBarPopup")
        return JBPopupFactory.getInstance()
            .createActionGroupPopup(
                "",
                actionGroup as ActionGroup,
                context,
                JBPopupFactory.ActionSelectionAid.SPEEDSEARCH,
                false
            );
    }

    override fun createInstance(project: Project): StatusBarWidget {
        return CopilotStatusBarWidget(project);
    }

    override fun update(finishUpdate: Runnable?) {
        super.update(finishUpdate)
    }

}