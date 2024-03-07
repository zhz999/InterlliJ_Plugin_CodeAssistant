package code_assistant.editor

import code_assistant.settings.CodeAssistantSettingsState
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.IconLoader
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.util.containers.toArray
import code_assistant.window.ChatWindow
import code_assistant.settings.configuration.ConfigurationSettings
import java.util.*
import java.util.stream.Collectors
import javax.swing.*
import kotlin.collections.LinkedHashMap

/**
 * 动态注册 Editor ActionGroup
 */
class EditorActionsUtil {

    companion object {

        fun refreshActions() {
            val actionGroup = ActionManager.getInstance().getAction("action.editor.EditorActionGroup")
            if (actionGroup is DefaultActionGroup) {
                actionGroup.removeAll()
                actionGroup.addSeparator()

                val userGpt = CodeAssistantSettingsState.getInstance().gpt
                val configuredActions: Map<String, String> = ConfigurationSettings.getCurrentState().getTableData()
                configuredActions.forEach { (label, prompt) ->
                    val action: BaseEditorAction = object : BaseEditorAction(label, label) {
                        override fun actionPerformed(project: Project?, editor: Editor?, selectedText: String?) {

                            val message = prompt.replace("{{selectedCode}}", String.format(": %s ", selectedText))

                            val toolWindowManager = project?.let { ToolWindowManager.getInstance(it) }
                            if (toolWindowManager != null) {
                                val toolWindow = toolWindowManager.getToolWindow("开发助手")
                                if (toolWindow !== null) {
                                    toolWindow.show()
                                    val chatToolWindow = toolWindow.contentManager.findContent("Copilot")
                                    val panel = chatToolWindow.component
                                    if (panel is JPanel) {
                                        println("Find panel OK!")
                                        var outPane: JTextPane = JTextPane()
                                        for (i in 0 until panel.componentCount) {
                                            val jScrollPane = panel.getComponent(i)
                                            if (jScrollPane is JScrollPane) {
                                                println("Find jScrollPane OK!")
                                                for (k in 0 until jScrollPane.componentCount) {
                                                    val viewport = jScrollPane.viewport
                                                    if (viewport.view is JTextPane) {
                                                        val tmpPane = viewport.view as JTextPane
                                                        println(tmpPane.name)
                                                        if (tmpPane.name == "issue") {
                                                            tmpPane.text = message
                                                        }
                                                        if (tmpPane.name == "out") {
                                                            outPane = tmpPane
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        for (i in 0 until panel.componentCount) {
                                            val downPanel = panel.getComponent(i)
                                            if (downPanel is JPanel) {
                                                println("Find downPanel OK!")
                                                for (j in 0 until downPanel.componentCount) {
                                                    val buttonPanel = downPanel.getComponent(j)
                                                    if (buttonPanel is JPanel) {
                                                        println("Find buttonPanel OK!")
                                                        var submitBtn = JButton()
                                                        for (n in 0 until buttonPanel.componentCount) {
                                                            val submitButton = buttonPanel.getComponent(n)
                                                            if (submitButton is JButton) {
                                                                println("Find submitBtn OK!")
                                                                submitBtn = submitButton
                                                            }
                                                        }
                                                        for (n in 0 until buttonPanel.componentCount) {
                                                            val jTextField = buttonPanel.getComponent(n)
                                                            if (jTextField is JTextField) {
                                                                println("Find jTextField OK!")

                                                                buttonPanel.setEnabled(false)
                                                                submitBtn.setEnabled(false);
                                                                jTextField.setEnabled(false);
                                                                submitBtn.icon =
                                                                    IconLoader.getIcon("/icons/dis-send.svg", javaClass)

                                                                println("userGpt: $userGpt")
                                                                // 发送
                                                                if (userGpt == "Dorado") {
                                                                    ChatWindow.send(
                                                                        panel,
                                                                        buttonPanel,
                                                                        jTextField,
                                                                        message,
                                                                        outPane
                                                                    )
                                                                } else {
                                                                    ChatWindow().submit(
                                                                        outPane,
                                                                        message,
                                                                        panel,
                                                                        submitBtn,
                                                                        buttonPanel,
                                                                        jTextField
                                                                    )
                                                                }

                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    actionGroup.add(action)
                }
            }
        }

        fun registerOrReplaceAction(action: AnAction) {
            val actionManager = ActionManager.getInstance()
            val txt = action.templateText
            if (txt !== null) {
                val actionId: String = convertToId(txt)
                // val ext = actionManager.getAction(actionId)
                if (actionManager.getAction(actionId) != null) {
                    // println(actionId)
                    // actionManager.replaceAction(actionId, action)
                } else {
                    actionManager.registerAction(actionId, action, PluginId.getId("code_assistant"))
                }
            }
        }

        private fun convertToId(label: String): String {
            return "code.assistant.action.editor.configuration." + label.replace("\\s".toRegex(), "")
                .lowercase(Locale.getDefault()).trim()
        }

        var defaultActions: Map<String, String> = LinkedHashMap(
            java.util.Map.of(
                "查找Bugs",
                "Find bugs and output code with bugs fixed in the following code: {{selectedCode}}",
                "编写测试",
                "Write Tests for the selected code {{selectedCode}}",
                "解释",
                "Explain the selected code {{selectedCode}}",
                "重构",
                "Refactor the selected code {{selectedCode}}",
                "优化",
                "Optimize the selected code {{selectedCode}}"
            )
        )


        fun toArray(actionsMap: Map<String, String>): Array<Array<String?>> {
            return actionsMap.entries.stream().map { (key, value): Map.Entry<String?, String?> ->
                arrayOf(
                    key, value
                )
            }.collect(Collectors.toList()).toArray(Array(0) {
                arrayOfNulls<String>(
                    0
                )
            })
        }

    }

}

