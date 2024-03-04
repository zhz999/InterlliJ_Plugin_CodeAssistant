package statusbar;

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.ui.popup.ListPopup
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.impl.status.EditorBasedStatusBarPopup;
import icons.Icons

class CodeStatusBarWidget(project: Project) : EditorBasedStatusBarPopup(project, false) {

    override fun ID(): String {
        return "dev.statusbar.widget";
    }

    override fun getWidgetState(file: VirtualFile?): WidgetState {
        val state =  WidgetState("开发助手", "", true);
        state.icon = Icons.DefaultSmall;
        return state;
    }

    override fun createPopup(context: DataContext): ListPopup {
        val actionGroup=  ActionManager.getInstance().getAction("DevAction.statusBarPopup")
        return JBPopupFactory.getInstance()
            .createActionGroupPopup(
                "开发助手",
                actionGroup as ActionGroup,
                context,
                JBPopupFactory.ActionSelectionAid.SPEEDSEARCH,
                false);
    }

    override fun createInstance(project: Project): StatusBarWidget {
        return  CodeStatusBarWidget(project);
    }
}
