package com.zhz.bytedance.development_assistant_zhz

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.IconLoader
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.ColorUtil
import com.intellij.ui.JBColor
import com.intellij.ui.content.ContentFactory
import com.intellij.util.ui.JBUI
import settings.CodeAssistantSettingsState
import java.awt.*
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import javax.swing.*
import javax.swing.text.Style
import javax.swing.text.StyleConstants
import javax.swing.text.html.HTMLEditorKit

class ChatWindow : ToolWindowFactory {

    private val settings: CodeAssistantSettingsState = CodeAssistantSettingsState.getInstance()

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        try {
            val contentFactory = ContentFactory.getInstance()
            val component = toolWindow.component
            val size = SwingUtilities.getRoot(component).size
            val width = size.width
            val height = size.height
            val content = contentFactory.createContent(createToolWindowPanel(width, height), "Chat", false)
            content.icon = IconLoader.getIcon("/icons/codegpt-small.svg", javaClass)
            toolWindow.contentManager.addContent(content)


            println("toolWindow.width ===> $width")
            println("toolWindow.height ===> $height")
            println("content.width ===> " + SwingUtilities.getRoot(content.component).size.width)
            println("content.height ===> " + SwingUtilities.getRoot(content.component).size.height)

        } catch (e: IOException) {
            Messages.showErrorDialog(project, e.stackTraceToString(), "打开插件失败")
        }
    }

    private fun createToolWindowPanel(width: Int, height: Int): JPanel {
        val panel = JPanel(BorderLayout())

        // 问题
        val issuePane = JTextPane()
        issuePane.name = "issue"
        issuePane.contentType = "text/html"
        issuePane.editorKit = HTMLEditorKit()
        issuePane.isEditable = false
        val issuePaneFont: Font = issuePane.font
        issuePane.setFont(issuePaneFont.deriveFont(14f));
        issuePane.text = "Ask me anything..."
        issuePane.setMargin(JBUI.insets(10, 5));
        val scrollIssuePanePane = JScrollPane(issuePane)
        scrollIssuePanePane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollIssuePanePane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollIssuePanePane.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, JBColor.DARK_GRAY))
        panel.add(scrollIssuePanePane, BorderLayout.NORTH)


        // 解释
        val textPane = JTextPane()
        val textPanePreferredSize = Dimension(width, (height * 0.8).toInt())
        textPane.name = "out"
        textPane.contentType = "text/html"
        textPane.editorKit = HTMLEditorKit()
        textPane.isEditable = false
        val font: Font = textPane.font
        textPane.setFont(font.deriveFont(10f));
        textPane.setMargin(JBUI.insets(8, 5));
        textPane.size = textPanePreferredSize
        textPane.preferredSize = textPanePreferredSize
        val scrollPane = JScrollPane(textPane)
        scrollPane.isDoubleBuffered = true
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        panel.add(scrollPane, BorderLayout.CENTER)

        // 输入
        val jTextField = JTextField(33)
        jTextField.setBackground(ColorUtil.fromHex("#ff000000"))
        jTextField.setBorder(BorderFactory.createEmptyBorder())
        // 提交按钮
        val submitButton = JButton(IconLoader.getIcon("/icons/send.svg", javaClass));
        submitButton.setContentAreaFilled(false);
        submitButton.setBorder(BorderFactory.createEmptyBorder())
        submitButton.preferredSize = Dimension(40, 28) // 宽度为100像素，高度为50像素

        // 输入文本框 Pane
        val buttonPanel = JPanel()
        buttonPanel.setLayout(BorderLayout())
        buttonPanel.setBorder(RoundBorder(10));
        buttonPanel.add(jTextField, BorderLayout.WEST)
        buttonPanel.add(submitButton, BorderLayout.EAST)
        buttonPanel.minimumSize = Dimension(Int.MAX_VALUE, 46)

        // 工具Panel
        val toolPanel = JPanel()
        toolPanel.setLayout(BorderLayout())
        toolPanel.minimumSize = Dimension(Int.MAX_VALUE, 46)
        val ll = DefaultComboBoxModel<String>()
        ll.addElement("mistral")
        ll.addElement("codellama")
        ll.addElement("gemma:7b")
        val modelList = JComboBox(ll)
        modelList.selectedItem = settings.model
        modelList.setBackground(ColorUtil.fromHex("#ff000000"))
        modelList.setBorder(BorderFactory.createEmptyBorder())
        toolPanel.add(modelList, BorderLayout.EAST)
        modelList.addActionListener {
            settings.model =  modelList.getItemAt(modelList.getSelectedIndex())
        }


        val downPanel = JPanel()
        downPanel.setLayout(BorderLayout())//上下布局
        downPanel.add(toolPanel, BorderLayout.NORTH)//上
        downPanel.add(buttonPanel, BorderLayout.SOUTH)//下


        panel.add(downPanel, BorderLayout.SOUTH)

        // 添加回车事件监听器
        jTextField.addActionListener { // 在这里处理回车事件的逻辑
            buttonPanel.setEnabled(false)
            submitButton.setEnabled(false);
            jTextField.setEnabled(false);
            submitButton.icon = IconLoader.getIcon("/icons/dissend.svg", javaClass)
            issuePane.text = jTextField.text
            submit(textPane, jTextField.text, panel, submitButton, buttonPanel, jTextField)
            jTextField.text = ""
        }

        // 按钮事件
        submitButton.addActionListener {
            buttonPanel.setEnabled(false)
            submitButton.setEnabled(false);
            jTextField.setEnabled(false);
            submitButton.icon = IconLoader.getIcon("/icons/dissend.svg", javaClass)
            issuePane.text = jTextField.text
            submit(textPane, jTextField.text, panel, submitButton, buttonPanel, jTextField)
            jTextField.text = ""
        }

        return panel
    }

    fun submit(
        textPane: JTextPane,
        inputText: String,
        panel: JPanel,
        submitButton: JButton,
        buttonPanel: JPanel,
        jTextField: JTextField
    ) {
        // 创建并执行后台任务
        val doc = textPane.styledDocument
        val style: Style = doc.addStyle("default", null)
        StyleConstants.setFontSize(style, 10);
        val settings: CodeAssistantSettingsState = CodeAssistantSettingsState.getInstance()
//        val postData = "{\"model\": \"${settings.model}\",\"messages\": [{\"role\": \"user\",\"content\": \"`${inputText}`\"}]}"

        val messagesObject = JsonObject()
        messagesObject.addProperty("role","user")
        messagesObject.addProperty("content","```$inputText```")
        val postData = JsonObject()
        postData.addProperty("model", settings.model)
        postData.add("messages", Gson().toJsonTree(arrayOf(messagesObject)).asJsonArray)

        println(postData)

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
                    outputStream.write(Gson().toJson(postData).toByteArray())
                    outputStream.flush()
                    outputStream.close()

                    // 获取响应数据
                    val responseCode = connection.responseCode
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        publish("begin ++++++++++++++++++++++ Chat: $inputText ++++++++++++++++++++ \r\n\n")
                        val inputStream = connection.inputStream
                        val reader = BufferedReader(InputStreamReader(inputStream,"utf-8"))
                        var line: String?
                        while (reader.readLine().also { line = it } != null) {
                            val gson = Gson()
                            // 将字符串转换为JSON对象
                            val jsonObject = gson.fromJson(line, JsonObject::class.java)
                            println(
                                jsonObject.get("done").asBoolean.toString() + " => " + jsonObject.get("message").asJsonObject.get(
                                    "content"
                                ).asString
                            )
                            val context = jsonObject.get("message").asJsonObject.get("content").asString
                            publish(context)
                            if (jsonObject.get("done").asBoolean) {
                                jTextField.setEnabled(true);
                                buttonPanel.setEnabled(true)
                                submitButton.setEnabled(true);
                                submitButton.icon = IconLoader.getIcon("/icons/send.svg", javaClass)
                                publish("\r\n\n+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ end \n\n\n")
                            }
                        }
                        reader.close()
                    } else {
                        jTextField.setEnabled(true);
                        buttonPanel.setEnabled(true)
                        submitButton.setEnabled(true);
                        submitButton.icon = IconLoader.getIcon("/icons/send.svg", javaClass)
                        JOptionPane.showMessageDialog(panel, "Request Failed：${connection.responseMessage}")
                    }
                } catch (ex: Exception) {
                    jTextField.setEnabled(true);
                    buttonPanel.setEnabled(true)
                    submitButton.setEnabled(true);
                    submitButton.icon = IconLoader.getIcon("/icons/send.svg", javaClass)
                    println(ex.localizedMessage)
                    JOptionPane.showMessageDialog(panel, "Request Exception：${ex.localizedMessage}")
                }
                return null
            }

            override fun process(chunks: MutableList<String>?) {
                for (chunk in chunks!!) {
//                    val stringAsByteArray = chunk.toByteArray()
//                    val utf8String = String(stringAsByteArray, Charsets.UTF_8)
                    doc.insertString(doc.length,  chunk, style);
                }
            }
        }
        worker.execute()
    }
}


