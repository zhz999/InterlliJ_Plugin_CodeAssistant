package code_assistant.editor

import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.IconLoader
import com.intellij.openapi.wm.ToolWindowManager
import code_assistant.settings.CodeAssistantSettingsConfigurable
import javax.swing.Icon

/**
 * 注册 Editor 工具栏
 */
class EditorPopupMenu1 : ActionGroup() {
    override fun getChildren(e: AnActionEvent?): Array<AnAction> {
        EditorActionsUtil.refreshActions()
        // 创建子菜单项
        return arrayOf(
            MyAction("Open Assistant","Open Assistant",IconLoader.getIcon("/icons/app-icon-off.svg", javaClass)),
            MyAction("Open Settings","Open Settings",IconLoader.getIcon("/icons/app-icon-off.svg", javaClass)),
            Separator.getInstance(),
        )
    }
}


class MyAction(text: String, description: String, icon: Icon) : AnAction(text, description, icon) {

    private var name = text
    override fun actionPerformed(e: AnActionEvent) {
       if(this.name == "Open Assistant") newChat(e)
       if(this.name == "Open Settings") openSettings(e)
    }

    private fun newChat(event: AnActionEvent) {
        val project: Project? = event.project
        if (project != null) {
            val toolWindowManager = ToolWindowManager.getInstance(project)
            val toolWindow = toolWindowManager.getToolWindow("开发助手")
            toolWindow?.show(null)
        }
    }


    private fun openSettings(event: AnActionEvent) {
        val project: Project? = event.project
        if (project != null) {
            ShowSettingsUtil.getInstance()
                .showSettingsDialog(project, CodeAssistantSettingsConfigurable::class.java)
        }
    }
}

