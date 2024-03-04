package editor

import com.intellij.ide.BrowserUtil
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.IconLoader
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.util.containers.toArray
import com.zhz.bytedance.development_assistant_zhz.ChatWindow
import settings.configuration.ConfigurationSettings
import java.net.URISyntaxException
import java.util.*
import java.util.stream.Collectors
import javax.swing.*
import javax.swing.event.HyperlinkEvent
import kotlin.collections.LinkedHashMap

class EditorActionsUtil {


    companion object {

        fun refreshActions() {
            val actionGroup = ActionManager.getInstance().getAction("action.editor.EditorActionGroup")
            if (actionGroup is DefaultActionGroup) {
                actionGroup.removeAll()
                actionGroup.addSeparator()
                val configuredActions: Map<String, String> = ConfigurationSettings.getCurrentState().getTableData()
                configuredActions.forEach { (label, prompt) ->
                    val action: BaseEditorAction = object : BaseEditorAction(label, label) {
                        override fun actionPerformed(project: Project?, editor: Editor?, selectedText: String?) {

                            val message =
                                prompt.replace("{{selectedCode}}", String.format(": %s", selectedText))

                            val toolWindowManager = project?.let { ToolWindowManager.getInstance(it) }
                            if (toolWindowManager != null) {
                                val toolWindow = toolWindowManager.getToolWindow("开发助手")
                                if (toolWindow !== null) {
                                    toolWindow.show()
                                    val chatToolWindow = toolWindow.contentManager.findContent("Chat")
                                    val panel = chatToolWindow.component
                                    if (panel is JPanel) {
                                        println("Find panel OK!")
                                        var outPane: JTextPane = JTextPane()
                                        for (i in 0 until panel.componentCount) {
                                            val jScrollPane = panel.getComponent(i)
                                            if(jScrollPane is JScrollPane){
                                                println("Find jScrollPane OK!")
                                                for (k in 0 until jScrollPane.componentCount) {
                                                    val viewport = jScrollPane.viewport
                                                    if (viewport.view is JTextPane) {
                                                        val tmpPane =  viewport.view as JTextPane
                                                        println(tmpPane.name)
                                                        if(tmpPane.name == "issue"){
                                                            tmpPane.text = message
                                                        }
                                                        if(tmpPane.name == "out"){
                                                            outPane = tmpPane
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        for (i in 0 until panel.componentCount) {
                                            val downPanel = panel.getComponent(i)
                                            if(downPanel is  JPanel){
                                                println("Find downPanel OK!")
                                                for (j in 0 until downPanel.componentCount) {
                                                    val buttonPanel = downPanel.getComponent(j)
                                                    if(buttonPanel is  JPanel){
                                                        println("Find buttonPanel OK!")
                                                        var submitBtn = JButton()
                                                        for (n in 0 until buttonPanel.componentCount) {
                                                            val submitButton = buttonPanel.getComponent(n)
                                                            if(submitButton is JButton){
                                                                println("Find submitBtn OK!")
                                                                submitBtn = submitButton
                                                            }
                                                        }
                                                        for (n in 0 until buttonPanel.componentCount) {
                                                            val jTextField = buttonPanel.getComponent(n)
                                                            if(jTextField is  JTextField){
                                                                println("Find jTextField OK!")

                                                                buttonPanel.setEnabled(false)
                                                                submitBtn.setEnabled(false);
                                                                jTextField.setEnabled(false);
                                                                submitBtn.icon = IconLoader.getIcon("/icons/dissend.svg", javaClass)

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
                    actionGroup.add(action)
                }
            }
        }


        fun registerOrReplaceAction(action: AnAction) {
            val actionManager = ActionManager.getInstance()
            val txt = action.templateText
            if(txt !== null){
                val actionId: String = convertToId(txt)
                val ext = actionManager.getAction(actionId)
                println(ext)
                if (actionManager.getAction(actionId) != null) {
                    actionManager.replaceAction(actionId, action)
                } else {
                    actionManager.registerAction(actionId, action, PluginId.getId("CodeAssistant"))
                }
            }
        }


        private fun convertToId(label: String): String {
            return "settings.configuration." + label.replace("\\s".toRegex(), "").lowercase(Locale.getDefault()).trim()
        }

        var defaultActions: Map<String, String> = LinkedHashMap(
            java.util.Map.of(
                "CA:Find Bugs", "Find bugs and output code with bugs fixed in the following code: {{selectedCode}}",
                "CA:Write Tests", "Write Tests for the selected code {{selectedCode}}",
                "CA:Explain", "Explain the selected code {{selectedCode}}",
                "CA:Refactor", "Refactor the selected code {{selectedCode}}",
                "CA:Optimize", "Optimize the selected code {{selectedCode}}"
            )
        )

        var defaultActionsArray = toArray(defaultActions)

        fun toArray(actionsMap: Map<String, String>): Array<Array<String?>> {
            return actionsMap.entries
                .stream()
                .map { (key, value): Map.Entry<String?, String?> ->
                    arrayOf(
                        key,
                        value
                    )
                }
                .collect(Collectors.toList())
                .toArray(Array(0) {
                    arrayOfNulls<String>(
                        0
                    )
                })
        }

        fun handleHyperlinkClicked(event: HyperlinkEvent) {
            val url = event.url
            if (HyperlinkEvent.EventType.ACTIVATED == event.eventType && url != null) {
                try {
                    BrowserUtil.browse(url.toURI())
                } catch (e: URISyntaxException) {
                    throw RuntimeException(e)
                }
            }
        }
    }

}

