package code_assistant.settings

import com.intellij.openapi.application.Application
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.wm.WindowManager
import com.intellij.ui.JBColor
import com.intellij.ui.TitledSeparator
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextArea
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.LabelPosition
import com.intellij.ui.dsl.builder.RowLayout
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.gridLayout.UnscaledGaps
import com.intellij.util.ui.FormBuilder
import com.intellij.util.ui.JBUI
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import org.jetbrains.annotations.NotNull
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.Insets
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import javax.swing.*

class CodeAssistantSettingsComponent {
    private lateinit var myMainPanel: JPanel
    private val myUserNameText = JBTextField()
    private val model = JBTextField()
    private val uri = JBTextField()
    private var token = JEditorPane()//JBTextArea("", 7, 68)
    private val sessionId = JBTextField()
    private val parentMessageId = JBTextField()
    private val language = JBTextField()
    private var enabled = JBCheckBox("启用字节 Dorado Copilot 窗口 ?")
    private val submitButton = JButton("Test Connection")
    private val socketButton = JButton("Test Connection")
    val restartButton = JButton("Restart IDEA")
    private val gpt = ComboBox(arrayOf("Ollama", "Copilot"))

    fun getPanel(): JPanel {
        val settings: CodeAssistantSettingsState = CodeAssistantSettingsState.getInstance()
        restartButton.isVisible = false
        restartButton.foreground = Color.decode("#5083FB")

        val jPanel = JPanel(BorderLayout())
        jPanel.add(enabled,BorderLayout.WEST)
        jPanel.add(restartButton,BorderLayout.EAST)
        token.background = JBColor.WHITE
        token.setMargin(JBUI.insets(10, 8))
//        token.preferredSize =Dimension(100,80)
//        token.lineWrap = true
//        token.wrapStyleWord = false
        // Copilot 面板
//        val panel = panel {
//            threeColumnsRow({
//                cell(sessionId).label("SessionId:",LabelPosition.TOP)
//                cell(parentMessageId).label("ParentMessageId:",LabelPosition.TOP)
//                cell(language).label("Language:",LabelPosition.TOP)
//            })
//            row {
//                cell(token)
//                    .label("Token:",LabelPosition.TOP)
//                    .customize(UnscaledGaps(10,6,10,6))
//                    .comment("这里使用的是 https://data.bytedance.net/dorado 的 Token")
//            }.layout(RowLayout.INDEPENDENT)
//        }
        // 设置面板
        myMainPanel = FormBuilder.createFormBuilder()
            // Ollama Settings
            .addComponent(TitledSeparator("Ollama Settings"))
            .addLabeledComponent(JBLabel("Enter your name: "), myUserNameText, 1, false)
            .addLabeledComponent(JBLabel("Enter ollama uri: "), uri, 1, false)
            .addLabeledComponent(JBLabel("Enter ollama model: "), model, 1, false)
            .addComponent(submitButton, 1)
            // Copilot Settings
            .addVerticalGap(6)
            .addComponent(TitledSeparator("Dorado Copilot Settings"))
            .addLabeledComponent(JBLabel("SessionId: "), sessionId, 1, false)
            .addLabeledComponent(JBLabel("ParentMessageId: "), parentMessageId, 1, false)
            .addLabeledComponent(JBLabel("Language: "), language, 1, false)
            .addLabeledComponent(JBLabel("Token: "), token, 6, false)
//            .addLabeledComponent(
//                "",
//                panel,
//                4,
//                false
//            )
            .addLabeledComponent(socketButton, jPanel, 3, false)
            // Editor Action Settings
            .addVerticalGap(6)
            .addComponent(TitledSeparator("Select Editor Action Use Model Channel"))
            .addComponent(gpt, 1)
            .addComponentFillVertically(JPanel(), 0)
            .panel


        restartButton.addActionListener {
            val application: Application = ApplicationManager.getApplication()
            val windowManager = WindowManager.getInstance()
            for (win in windowManager.allProjectFrames) {
                println(win.project?.name)
                println(win.component?.name)
            }
            val flag = JOptionPane.showConfirmDialog(
                myMainPanel,
                "配置变更，是否需求重启后生效？",
                "IDEA 重启消息", JOptionPane.YES_NO_OPTION
            )
            //
            if (flag == 0) {
                application.invokeLater({ application.restart() }, ModalityState.nonModal())
            }
        }


        // 按钮事件
        socketButton.addActionListener {
            socketButton.setEnabled(false);
            println(settings.token)
            val wsClient = object :
                WebSocketClient(URI("wss://data.bytedance.net/socket-dorado/copilot/v1/socket?token=" + settings.token)) {
                override fun onOpen(handshakedata: ServerHandshake?) {
                    JOptionPane.showMessageDialog(myMainPanel, "connection success")
                    close(1000)
                    socketButton.setEnabled(true);
                }

                override fun onMessage(message: String?) {}

                override fun onClose(code: Int, reason: String?, remote: Boolean) {
                    close()
                    socketButton.setEnabled(true);
                    println("connection failed: $reason")
                    if (code != 1000) {
                        JOptionPane.showMessageDialog(myMainPanel, "connection failed:$reason")
                    }
                }

                override fun onError(ex: java.lang.Exception?) {
                    close()
                    socketButton.setEnabled(true);
                    println("connection failed: ${ex?.localizedMessage}")
                }
            }
            wsClient.connect()
        }
        submitButton.addActionListener {
            submitButton.setEnabled(false);
            val postData =
                "{\"model\": \"${settings.model}\",\"messages\": [{\"role\": \"user\",\"content\": \"Test\"}]}"
            val worker = object : SwingWorker<Void, String>() {
                override fun doInBackground(): Void? {
                    try {
                        val url = URL(settings.uri)
                        val connection = url.openConnection() as HttpURLConnection
                        connection.requestMethod = "POST"
                        connection.doOutput = true
                        connection.setRequestProperty("Content-Type", "application/json")

                        // 发送POST数据
                        val outputStream = connection.outputStream
                        outputStream.write(postData.toByteArray())
                        outputStream.flush()
                        outputStream.close()

                        // 获取响应数据
                        val responseCode = connection.responseCode
                        if (responseCode == HttpURLConnection.HTTP_OK) {
                            submitButton.setEnabled(true);
                            JOptionPane.showMessageDialog(myMainPanel, "connection success")
                        } else {
                            submitButton.setEnabled(true);
                            JOptionPane.showMessageDialog(myMainPanel, "connection failed")
                        }
                    } catch (ex: Exception) {
                        submitButton.setEnabled(true);
                        JOptionPane.showMessageDialog(myMainPanel, "connection failed")
                    }
                    return null
                }

                override fun process(chunks: MutableList<String>?) {}
            }
            worker.execute()
        }
        return myMainPanel
    }


    fun getPreferredFocusedComponent(): JComponent {
        return myUserNameText
    }

    @NotNull
    fun getUserNameText(): String {
        return myUserNameText.getText()
    }

    fun setUserNameText(@NotNull newText: String?) {
        myUserNameText.text = newText
    }

    @NotNull
    fun getModel(): String {
        return model.getText()
    }

    fun setModel(@NotNull newText: String?) {
        model.text = newText
    }

    @NotNull
    fun getUri(): String {
        return uri.getText()
    }

    fun setUri(@NotNull newText: String?) {
        uri.text = newText
    }

    fun getToken(): String {
        return token.getText()
    }

    fun setToken(newText: String) {
        token.text = newText
    }

    fun getEnabled(): Boolean {
        return enabled.isSelected
    }

    fun setEnabled(flag: Boolean) {
        enabled.isSelected = flag
    }

    fun getGpt(): String {
        return gpt.selectedItem as String
    }

    fun setGpt(selectedItem: String) {
        gpt.selectedItem = selectedItem
    }


    fun getSessionId(): String {
        return sessionId.getText()
    }

    fun setSessionId(newText: String) {
        sessionId.text = newText
    }


    fun getParentMessageId(): String {
        return parentMessageId.getText()
    }

    fun setParentMessageId(newText: String) {
        parentMessageId.text = newText
    }

    fun getLanguage(): String {
        return language.getText()
    }

    fun setLanguage(newText: String) {
        language.text = newText
    }

}

