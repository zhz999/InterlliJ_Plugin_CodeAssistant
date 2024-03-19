package code_assistant.settings

import code_assistant.common.DoradoCommon
import code_assistant.common.Message
import code_assistant.window.ChatWindow
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.JBColor
import com.intellij.ui.TitledSeparator
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import com.intellij.util.ui.JBUI
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import org.jetbrains.annotations.NotNull
import java.awt.BorderLayout
import java.awt.Color
import java.awt.event.ActionListener
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import javax.swing.*

class CodeAssistantSettingsComponent {
    private lateinit var myMainPanel: JPanel
    private val myUserNameText = JBTextField()
    private val model = JBTextField()
    private val uri = JBTextField()
    private var token = JEditorPane()
    private val sessionId = JBTextField()
    private val parentMessageId = JBTextField()
    private val language = JBTextField()
    private var enabled = JBCheckBox("启用字节 Dorado Copilot 窗口 ?")
    private val submitButton = JButton("Test Connection")
    private val socketButton = JButton("Test Connection")
    private val gpt = ComboBox(arrayOf("Ollama", "Copilot"))
    private val connectionResult0 = JBLabel()
    private val connectionResult = JBLabel()
    val restartResult = JBLabel()

    fun getPanel(): JPanel {
        val settings: CodeAssistantSettingsState = CodeAssistantSettingsState.getInstance()
        // 设置字体
        connectionResult0.font = connectionResult0.font.deriveFont(10f)
        connectionResult.font = connectionResult.font.deriveFont(10f)
        restartResult.font = restartResult.font.deriveFont(10f)
        // 设置字体颜色
        connectionResult0.foreground = Color.decode("#5083FB")
        connectionResult.foreground = Color.decode("#5083FB")
        // 设置重启提示语字体颜色
        restartResult.foreground = Color.decode("#40B6E0")

        val jPanel = JPanel(BorderLayout())
        jPanel.add(enabled, BorderLayout.WEST)
        jPanel.add(restartResult, BorderLayout.EAST)

        val tPanel = FormBuilder.createFormBuilder()
        token = JEditorPane()
        token.background = JBColor.WHITE
        token.margin = JBUI.insets(20, 10)
        token.border = JBUI.Borders.customLine(JBColor.decode("#dee0e3"))
        token.text = "启用字节 Dorado Copilot 窗口 ?"
        tPanel.setFormLeftIndent(3)
        tPanel.addComponent(token, 4)
        tPanel.addComponent(connectionResult, 6)

        // 设置面板
        myMainPanel = FormBuilder.createFormBuilder()
            // Ollama Settings
            .addComponent(TitledSeparator("Ollama Settings"))
            .addLabeledComponent(JBLabel("Enter your name: "), myUserNameText, 1, false)
            .addLabeledComponent(JBLabel("Enter ollama uri: "), uri, 1, false)
            .addLabeledComponent(JBLabel("Enter ollama model: "), model, 1, false)
            .addLabeledComponent(submitButton, connectionResult0, 3, false)
            // Copilot Settings
            .addVerticalGap(6)
            .addComponent(TitledSeparator("Dorado Copilot Settings"))
            .addLabeledComponent(JBLabel("SessionId: "), sessionId, 1, false)
            .addLabeledComponent(JBLabel("ParentMessageId: "), parentMessageId, 1, false)
            .addLabeledComponent(JBLabel("Language: "), language, 1, false)
            .addLabeledComponent(JBLabel("Token: "), tPanel.panel, 6, false)
            .addLabeledComponent(socketButton, jPanel, 3, false)
            // Editor Action Settings
            .addVerticalGap(6)
            .addComponent(TitledSeparator("Select Editor Action Use Model Channel"))
            .addComponent(gpt, 1)
            .addComponentFillVertically(JPanel(), 0)
            .panel

        // 按钮事件
        socketButton.addActionListener {
            socketButton.isEnabled = false;

            val token = DoradoCommon.getToken()
            if (token.isEmpty()) {
                Message.Error("获取Token失败")
            } else {
                ChatWindow.WsState.wsToken = token
                val wsClient = object :
                    WebSocketClient(URI("wss://data.bytedance.net/socket-dorado/copilot/v1/socket?token=$token")) {
                    override fun onOpen(handshakedata: ServerHandshake?) {
                        // Message.Info("connection success")
                        connectionResult.foreground = Color.decode("#62B543") // 62B543  & F26522
                        connectionResult.text = "Connection success"
                        close(1000)
                        socketButton.isEnabled = true;
                    }

                    override fun onMessage(message: String?) {}

                    override fun onClose(code: Int, reason: String?, remote: Boolean) {
                        close()
                        socketButton.isEnabled = true;
                        println("connection failed: $reason")
                        if (code != 1000) {
                            // Message.Error("connection failed:$reason")
                            connectionResult.foreground = Color.decode("#F26522") // 62B543  & F26522
                            connectionResult.text = "Connection failed:$reason"
                        }
                    }

                    override fun onError(ex: java.lang.Exception?) {
                        close()
                        socketButton.isEnabled = true;
                        println("connection failed: ${ex?.localizedMessage}")
                    }
                }
                wsClient.connect()
            }


        }
        submitButton.addActionListener {
            var counts = 0
            val timer = Timer(500, ActionListener {
                counts += 1
                val flag = counts % 2
                if (flag == 1) {
                    connectionResult0.text = "Connecting ..."
                } else {
                    connectionResult0.text = "Connecting ......"
                }
            })

            submitButton.isEnabled = false;
            val postData =
                "{\"model\": \"${settings.model}\",\"messages\": [{\"role\": \"user\",\"content\": \"Test\"}]}"
            val worker = object : SwingWorker<Void, String>() {
                override fun doInBackground(): Void? {
                    try {

                        // 启动或停止定时器
                        if (!timer.isRunning) {
                            timer.start()
                        }

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
                            submitButton.isEnabled = true;
                            // Message.Info("connection success")
                            if (timer.isRunning) {
                                timer.stop()
                            }
                            connectionResult0.foreground = Color.decode("#62B543") // 62B543  & F26522
                            connectionResult0.text = "Connection success"
                        } else {
                            submitButton.isEnabled = true;
                            // Message.Error("connection failed")
                            if (timer.isRunning) {
                                timer.stop()
                            }
                            connectionResult0.foreground = Color.decode("#F26522") // 62B543  & F26522
                            connectionResult0.text = "Connection failed"
                        }
                    } catch (ex: Exception) {
                        submitButton.isEnabled = true;
                        // Message.Error("connection exception")
                        if (timer.isRunning) {
                            timer.stop()
                        }
                        connectionResult0.foreground = Color.decode("#F26522") // 62B543  & F26522
                        connectionResult0.text = "Connection failed:${ex.localizedMessage}"
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
        return myUserNameText.text
    }

    fun setUserNameText(@NotNull newText: String?) {
        myUserNameText.text = newText
    }

    @NotNull
    fun getModel(): String {
        return model.text
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

