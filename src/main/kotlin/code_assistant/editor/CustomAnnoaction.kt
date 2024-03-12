package code_assistant.editor

import code_assistant.settings.CodeAssistantSettingsConfigurable
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowManager
import javax.swing.Icon


class CustomAnnoaction(text: String, description: String, icon: Icon) : AnAction(text, description, icon) {

    private var name = text
    override fun actionPerformed(e: AnActionEvent) {

        if (this.name == "Open Assistant") newChat(e)
        if (this.name == "Open Settings") openSettings(e)
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