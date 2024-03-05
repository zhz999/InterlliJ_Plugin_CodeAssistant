package code_assistant.settings

import com.intellij.openapi.application.Application
import com.intellij.openapi.application.ApplicationManager
import com.intellij.ui.TitledSeparator
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextArea
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.ui.FormBuilder
import com.intellij.util.ui.JBUI
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import org.jetbrains.annotations.NotNull
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import javax.swing.*

class CodeAssistantSettingsComponent {
    private lateinit var myMainPanel: JPanel
    private val myUserNameText = JBTextField()
    private val model = JBTextField()
    private val uri = JBTextField()
    private var dorado = JBTextArea("", 5, 80)
    private var enabledDorado = JBCheckBox("启用字节 Dorado ?")
    private val submitButton = JButton("Test Connection")
    private val socketButton = JButton("Test Connection")

    fun getPanel(): JPanel {
        val settings: CodeAssistantSettingsState = CodeAssistantSettingsState.getInstance()
        dorado.setBorder(JBUI.Borders.empty(10,8))
        dorado.lineWrap = true
        dorado.wrapStyleWord = false

        // 监听变化重启编辑器
        enabledDorado.addActionListener() {
            if (enabledDorado.isSelected) {
                JOptionPane.showMessageDialog(myMainPanel, "重启后生效")
                val application: Application = ApplicationManager.getApplication()
                application.restart()
            }
        }


        val panel = panel {
            row {
                cell(
                    dorado
                )
            }
            row { comment("这里使用的是 https://data.bytedance.net/dorado 的 Token") }
        }

        myMainPanel = FormBuilder.createFormBuilder()
            // Ollama settings
            .addComponent(TitledSeparator("Ollama settings"))
            .addLabeledComponent(JBLabel("Enter your name: "), myUserNameText, 1, false)
            .addLabeledComponent(JBLabel("Enter ollama uri: "), uri, 1, false)
            .addLabeledComponent(JBLabel("Enter ollama model: "), model, 1, false)
            .addComponent(submitButton, 1)
            // Dorado settings
            .addVerticalGap(6)
            .addComponent(TitledSeparator("Dorado settings"))
            .addLabeledComponent(
                "",
                panel,
                1,
                true
            )
            .addLabeledComponent(socketButton, enabledDorado, 3, false)

            .addComponentFillVertically(JPanel(), 0)
            .panel

        // 按钮事件
        socketButton.addActionListener {
            socketButton.setEnabled(false);
            println(settings.dorado)
            val wsClient = object :
                WebSocketClient(URI("wss://data.bytedance.net/socket-dorado/copilot/v1/socket?token=" + settings.dorado)) {
                override fun onOpen(handshakedata: ServerHandshake?) {
                    JOptionPane.showMessageDialog(myMainPanel, "connection success")
                    close(1000)
                    socketButton.setEnabled(true);
                }

                override fun onMessage(message: String?) {

                }

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

                override fun process(chunks: MutableList<String>?) {

                }
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

    fun getDorado(): String {
        return dorado.getText()
    }

    fun setDorado(newText: String) {
        dorado.text = newText
    }


    fun getEnabledDorado(): Boolean {
        return enabledDorado.isSelected
    }

    fun setEnabledDorado(flag: Boolean) {
        enabledDorado.isSelected = flag
    }

}