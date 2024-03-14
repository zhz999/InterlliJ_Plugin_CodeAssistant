package code_assistant.settings

import code_assistant.window.ChatWindow
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.options.ShowSettingsUtil

class ReCopilotAction : AnAction(
    "Copilot 重连",
    "Copilot 重连",
    null
) {
    override fun actionPerformed(e: AnActionEvent) {
        ChatWindow.reCopilot()
    }
}