package settings

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.intellij.openapi.util.IconLoader
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import org.jetbrains.annotations.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import javax.swing.*




class CodeAssistantSettingsComponent {
    private var myMainPanel: JPanel? = null
    private val myUserNameText = JBTextField()
    private val model = JBTextField()
    private val uri = JBTextField()
    private val myIdeaUserStatus = JBCheckBox("是否启用此设置? ")
    private val submitButton = JButton("Test Connection")

    fun getPanel(): JPanel? {
        val settings: CodeAssistantSettingsState = CodeAssistantSettingsState.getInstance()
        myMainPanel = FormBuilder.createFormBuilder()
            .addLabeledComponent(JBLabel("Ollama settings: "),JSeparator(),1, false)
            .addLabeledComponent(JBLabel("Enter your name: "), myUserNameText, 1, false)
            .addLabeledComponent(JBLabel("Enter ollama uri: "), uri, 1, false)
            .addLabeledComponent(JBLabel("Enter ollama model: "), model, 1, false)
            .addComponent(myIdeaUserStatus, 1)
            .addComponentToRightColumn(submitButton,1)
            .addComponentFillVertically(JPanel(), 0)
            .panel

        // 按钮事件
        submitButton.addActionListener {
            submitButton.setEnabled(false);
            val postData = "{\"model\": \"${settings.model}\",\"messages\": [{\"role\": \"user\",\"content\": \"Test\"}]}"
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

    fun getIdeaUserStatus(): Boolean {
        return myIdeaUserStatus.isSelected
    }

    fun setIdeaUserStatus(newStatus: Boolean) {
        myIdeaUserStatus.setSelected(newStatus)
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

}