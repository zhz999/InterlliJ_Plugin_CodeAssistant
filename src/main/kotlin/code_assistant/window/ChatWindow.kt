package code_assistant.window

import code_assistant.settings.CodeAssistantSettingsState
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.intellij.openapi.application.Application
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.IconLoader
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.ui.ColorUtil
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.content.Content
import com.intellij.ui.content.ContentFactory
import com.intellij.util.ui.JBUI
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import org.json.JSONObject
import java.awt.*
import java.awt.event.ActionListener
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import javax.swing.*
import javax.swing.text.BadLocationException
import javax.swing.text.Style
import javax.swing.text.StyleConstants
import javax.swing.text.html.HTMLDocument
import javax.swing.text.html.HTMLEditorKit


class ChatWindow : ToolWindowFactory {

    private val settings: CodeAssistantSettingsState = CodeAssistantSettingsState.getInstance()

    private var counts = 0

    object WsState {
        @JvmStatic
        @Volatile
        var wsClient: WebSocketClient? = null

        @JvmStatic
        @Volatile
        var wsTimer: Timer? = null

        @JvmStatic
        @Volatile
        var doradoContent: Content? = null
    }

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        try {
            val contentFactory = ContentFactory.getInstance()
            val component = toolWindow.component
            val size = SwingUtilities.getRoot(component).size
            val width = size.width
            val height = size.height
            val chatComponent = createToolWindowPanel(width, height, "Ollama", project, toolWindow)
            val chatContent = contentFactory.createContent(chatComponent, "Ollama", false)
            chatContent.icon = IconLoader.getIcon("/icons/ollama.svg", javaClass)
            toolWindow.contentManager.addContent(chatContent)
            if (settings.enabled) {
                val wsComponent = createToolWindowPanel(width, height, "Copilot", project, toolWindow)
                WsState.doradoContent = contentFactory.createContent(wsComponent, "Copilot", false)
                WsState.doradoContent!!.icon = IconLoader.getIcon("/icons/off-dorado.svg", javaClass)
                toolWindow.contentManager.addContent(WsState.doradoContent!!)
            }


        } catch (e: IOException) {
            Messages.showErrorDialog(project, e.stackTraceToString(), "打开插件失败")
        }
    }


    private fun createToolWindowPanel(
        width: Int, height: Int, displayName: String,
        project: Project, toolWindow: ToolWindow
    ): JPanel {
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
        val scrollIssuePanePane = JBScrollPane(issuePane)
        scrollIssuePanePane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollIssuePanePane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollIssuePanePane.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.decode("#dee0e3")))
        if (displayName == "Ollama") {
            // panel.add(scrollIssuePanePane, BorderLayout.NORTH)
        }

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
        val scrollPane = JBScrollPane(textPane)
        scrollPane.isDoubleBuffered = true
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

//        // 获取EditorFactory实例
//        val editorFactory = EditorFactory.getInstance()
//        // 创建一个Document对象，用于保存编辑器中的文本内容
//        val document = editorFactory.createDocument("")
//        // 创建编辑器
//        val editor = editorFactory.createEditor(document)
//        // 在Document中设置文本内容
//        editor.document.setText("Hello, Kotlin!")
//        scrollPane.add(editor.component)

        panel.add(scrollPane, BorderLayout.CENTER)

        // 输入
        val jTextField = JTextField(38)
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
        // buttonPanel.setBorder(RoundBorder(10));
        buttonPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, Color.decode("#dee0e3")))
        buttonPanel.add(jTextField, BorderLayout.WEST)
        buttonPanel.add(submitButton, BorderLayout.EAST)
        buttonPanel.preferredSize = Dimension(Int.MAX_VALUE, 40)

        // 工具Panel
        val toolPanel = JPanel()
        toolPanel.setLayout(BorderLayout())
        toolPanel.minimumSize = Dimension(Int.MAX_VALUE, 46)
        toolPanel.setBackground(JBColor.WHITE)
        toolPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 0, Color.decode("#dee0e3")))
        val ll = DefaultComboBoxModel<String>()
        ll.addElement("mistral")
        ll.addElement("codellama")
        ll.addElement("gemma:7b")
        val modelList = ComboBox(ll)
        modelList.selectedItem = settings.model
        modelList.preferredSize = Dimension(100, 35)
        // modelList.setBackground(ColorUtil.fromHex("#ff000000"))
        // modelList.setBorder(BorderFactory.createEmptyBorder())
        modelList.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, JBColor.WHITE))
        toolPanel.add(modelList, BorderLayout.EAST)
        modelList.addActionListener {
            settings.model = modelList.getItemAt(modelList.getSelectedIndex())
        }


        val downPanel = JPanel()
        downPanel.setLayout(BorderLayout())//上下布局

        if (displayName == "Ollama") {
            downPanel.add(toolPanel, BorderLayout.NORTH)//上
        }

        downPanel.add(buttonPanel, BorderLayout.SOUTH)//下
        panel.add(downPanel, BorderLayout.SOUTH)

        if (displayName == "Copilot") {
            WsState.wsTimer = Timer(500, ActionListener {
                counts += 1
                val flag = counts % 2
                if (flag == 1) {
                    submitButton.setEnabled(true);
                    submitButton.icon = IconLoader.getIcon("/icons/send.svg", javaClass)
                } else {
                    submitButton.setEnabled(false);
                    submitButton.icon = IconLoader.getIcon("/icons/dis-send.svg", javaClass)
                }
            })
            WsState.wsClient =
                connectWs(
                    textPane,
                    submitButton,
                    buttonPanel,
                    jTextField,
                    WsState.wsTimer!!,
                    scrollPane,
                    project,
                    toolWindow,
                )
        }

        // 添加回车事件监听器
        jTextField.addActionListener { // 在这里处理回车事件的逻辑
            issuePane.text = jTextField.text
            if (displayName == "Ollama") {
                submit(textPane, jTextField.text, panel, submitButton, buttonPanel, jTextField)
            } else {
                // textPane.text = ""
                sendMessage(buttonPanel, jTextField, textPane)
            }
            jTextField.text = ""
        }

        // 按钮事件
        submitButton.addActionListener {
            issuePane.text = jTextField.text
            if (displayName == "Ollama") {
                submit(textPane, jTextField.text, panel, submitButton, buttonPanel, jTextField)
            } else {
                // textPane.text = ""
                sendMessage(buttonPanel, jTextField, textPane)
            }
            jTextField.text = ""
        }

        return panel
    }

    private fun sendMessage(
        buttonPanel: JPanel,
        jTextField: JTextField,
        outPanel: JTextPane,
    ) {
        val doc = outPanel.styledDocument
        val style: Style = doc.addStyle("default", null)
        StyleConstants.setFontSize(style, 12);
        val htmlKit = HTMLEditorKit()
        try {
            val htmlContent = """
                <html>
                    <body>
                        <div style="border:'1px solid #dee0e3';border-radius:'10px';padding:'8px';background:'#dee0e3';">${jTextField.text}</div>
                        <br>
                    </body>
                </html>
            """.trimIndent()
            htmlKit.insertHTML(doc as HTMLDocument, doc.length, htmlContent, 0, 0, null)
        } catch (ex: BadLocationException) {
            ex.printStackTrace()
        }

        buttonPanel.setEnabled(false)
        jTextField.setEnabled(false);
        if (!WsState.wsTimer?.isRunning!!) {
            WsState.wsTimer!!.start()
        }
        val mes = parseMessage(jTextField.text)
        WsState.wsClient?.send(mes)
    }

    companion object {

        @JvmStatic
        fun parseMessage(prompt: String): String {

            val doradoCopilotConfig = CodeAssistantSettingsState.getInstance()

            val messagesObject = JSONObject()
            messagesObject.put("jsonrpc", "2.0")
            messagesObject.put("id", 1)
            messagesObject.put("method", "ai/conversation")

            val paramsData = JSONObject()
            paramsData.put("sessionId", doradoCopilotConfig.sessionId)
            paramsData.put("parentMessageId", doradoCopilotConfig.parentMessageId)

            val contentData = JSONObject()
            contentData.put("type", "text")
            contentData.put("message", prompt)
            if (doradoCopilotConfig.language.isNotEmpty()) {
                contentData.put("language", doradoCopilotConfig.language)
            }
            paramsData.put("content", contentData)

            val senderData = JSONObject()
            senderData.put("type", "chat")
            paramsData.put("sender", senderData)
            paramsData.put("model", "gpt-35-turbo")
            paramsData.put("temperature", 0)
            paramsData.put("maxToken", 4096)

            messagesObject.put("params", paramsData)
            println(messagesObject.toString())

            return messagesObject.toString()
        }

        @JvmStatic
        fun send(
            panel: JPanel,
            buttonPanel: JPanel,
            jTextField: JTextField,
            message: String,
            outPanel: JTextPane
        ) {
            val doc = outPanel.styledDocument
            val style: Style = doc.addStyle("default", null)
            StyleConstants.setFontSize(style, 12);
            val htmlKit = HTMLEditorKit()
            val styleSheet = htmlKit.styleSheet
            styleSheet.addRule("code { font-family: monospace; }")
            try {
                val htmlContent = """
                <html>
                    <body>
                        <div style="border:'1px solid #dee0e3';border-radius:'10px';padding:'8px';background:'#dee0e3';"><code>${message}</code></div>
                        <br>
                    </body>
                </html>
            """.trimIndent()
                htmlKit.insertHTML(doc as HTMLDocument, doc.length, htmlContent, 0, 0, null)
            } catch (ex: BadLocationException) {
                ex.printStackTrace()
            }
            buttonPanel.setEnabled(false)
            jTextField.setEnabled(false);
            if (!WsState.wsTimer?.isRunning!!) {
                WsState.wsTimer!!.start()
            }
            val mes = parseMessage(message)
            if (WsState.wsClient == null) {
                JOptionPane.showMessageDialog(panel, "Request Exception：wsClient is Closed !")
                return
            }
            WsState.wsClient?.send(mes)
        }
    }


    private fun setStatus(project: Project, toolWindow: ToolWindow, status: String) {
        var ico = "/icons/app-icon-off.svg"
        if (status == "Success") {
            ico = "/icons/app-icon-online.svg"
        }
        val icon: Icon = IconLoader.getIcon(ico, javaClass)
        val application: Application = ApplicationManager.getApplication()
        application.invokeLater { toolWindow.setIcon(icon) }
    }


    private fun connectWs(
        textPane: JTextPane,
        submitButton: JButton,
        buttonPanel: JPanel,
        jTextField: JTextField,
        timer: Timer,
        scrollPane: JBScrollPane,
        project: Project,
        toolWindow: ToolWindow
    ): WebSocketClient {

        val wsClient: WebSocketClient = object :
            WebSocketClient(URI("wss://data.bytedance.net/socket-dorado/copilot/v1/socket?token=" + settings.token)) {
            override fun onOpen(handshakedata: ServerHandshake?) {
                println("WebSocketClient Connect Success")
                WsState.doradoContent!!.icon = IconLoader.getIcon("/icons/dorado.svg", javaClass)
                setStatus(project, toolWindow, "Success")
            }

            override fun onMessage(message: String?) {
                val doc = textPane.styledDocument
                val verticalScrollBar = scrollPane.verticalScrollBar
                verticalScrollBar.value = verticalScrollBar.maximum
                val style: Style = doc.addStyle("default", null)
                StyleConstants.setFontSize(style, 10);
                println(message)
                val content = JSONObject(message).getJSONObject("result").getJSONObject("content")
                if (content.has("type") && content.getString("type") == "answer_end") {
                    println("=========== answer_end")
                    jTextField.setEnabled(true);
                    buttonPanel.setEnabled(true)
                    submitButton.setEnabled(true);
                    if (timer.isRunning) {
                        timer.stop()
                        submitButton.icon = IconLoader.getIcon("/icons/send.svg", javaClass)
                    }
                }
                if (content.has("part")) {
                    doc.insertString(doc.length, content.getString("part"), style);
                }
            }

            override fun onClose(code: Int, reason: String?, remote: Boolean) {
                WsState.doradoContent!!.icon = IconLoader.getIcon("/icons/dorado.svg", javaClass)
                setStatus(project, toolWindow, "Closed")
                println("WebSocketClient Connect onClose:$reason")
                if (timer.isRunning) {
                    timer.stop()
                    submitButton.icon = IconLoader.getIcon("/icons/send.svg", javaClass)
                }
                jTextField.setEnabled(true);
                buttonPanel.setEnabled(true)
                submitButton.setEnabled(true);
            }

            override fun onError(ex: java.lang.Exception?) {
                println("WebSocketClient Connect onError:${ex?.stackTraceToString()}")
                WsState.doradoContent!!.icon = IconLoader.getIcon("/icons/dorado.svg", javaClass)
                setStatus(project, toolWindow, "Exception")
                if (timer.isRunning) {
                    timer.stop()
                    submitButton.icon = IconLoader.getIcon("/icons/send.svg", javaClass)
                }
                jTextField.setEnabled(true);
                buttonPanel.setEnabled(true)
                submitButton.setEnabled(true);
            }
        }
        wsClient.connect()
        return wsClient
    }


//    https://github.com/ollama/ollama/blob/main/docs/api.md
    fun submit(
        textPane: JTextPane,
        inputText: String,
        panel: JPanel,
        submitButton: JButton,
        buttonPanel: JPanel,
        jTextField: JTextField
    ) {
        buttonPanel.setEnabled(false)
        jTextField.setEnabled(false);
        var counts = 0
        // 创建一个定时器，用于模拟加载过程
        val timer = Timer(500, ActionListener {
            counts += 1
            // 模拟加载过程
            //submitButton.isVisible = !submitButton.isVisible
            val flag = counts % 2
            if (flag == 1) {
                submitButton.setEnabled(true);
                submitButton.icon = IconLoader.getIcon("/icons/send.svg", javaClass)
            } else {
                submitButton.setEnabled(false);
                submitButton.icon = IconLoader.getIcon("/icons/dis-send.svg", javaClass)
            }
        })

        // 启动或停止定时器
        if (!timer.isRunning) {
            timer.start()
        }

        // 创建并执行后台任务
        val doc = textPane.styledDocument
        val style: Style = doc.addStyle("default", null)
        StyleConstants.setFontSize(style, 10);
        val settings: CodeAssistantSettingsState = CodeAssistantSettingsState.getInstance()
        val messagesObject = JsonObject()
        messagesObject.addProperty("role", "user")
        messagesObject.addProperty("content", inputText)
        val postData = JsonObject()
        postData.addProperty("model", settings.model)

        val options = JsonObject()
        options.addProperty("num_ctx", 4096)
        options.addProperty("temperature", 0.99)
        options.addProperty("top_k", 100)
        options.addProperty("top_p", 0.95)
        postData.add("options", options)

        postData.add("messages", Gson().toJsonTree(arrayOf(messagesObject)).asJsonArray)
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
                        //publish("begin ++++++++++++++++++++++ Ollama: $inputText ++++++++++++++++++++ \r\n\n")
                        val htmlKit = HTMLEditorKit()
                        try {
                            val htmlContent =
                                """<html><body><div style="border:'1px solid #dee0e3';border-radius:'10px';padding:'8px';background:'#dee0e3';">$inputText</div>
                                                 <br></body></html>
                                             """.trimIndent()
                            htmlKit.insertHTML(doc as HTMLDocument, doc.length, htmlContent, 0, 0, null)
                        } catch (ex: BadLocationException) {
                            ex.printStackTrace()
                        }
                        val inputStream = connection.inputStream
                        val reader = BufferedReader(InputStreamReader(inputStream, "utf-8"))
                        var line: String?
                        while (reader.readLine().also { line = it } != null) {
                            val gson = Gson()
                            val jsonObject = gson.fromJson(line, JsonObject::class.java)
                            val context = jsonObject.get("message").asJsonObject.get("content").asString
                            publish(context)
                            if (jsonObject.get("done").asBoolean) {
                                jTextField.setEnabled(true);
                                buttonPanel.setEnabled(true)
                                submitButton.setEnabled(true);
                                publish("\r\n\n\n\n\n")
                                if (timer.isRunning) {
                                    timer.stop()
                                    submitButton.icon = IconLoader.getIcon("/icons/send.svg", javaClass)
                                }
                            }
                        }
                        reader.close()
                    } else {
                        if (timer.isRunning) {
                            timer.stop()
                            submitButton.icon = IconLoader.getIcon("/icons/send.svg", javaClass)
                        }
                        jTextField.setEnabled(true);
                        buttonPanel.setEnabled(true)
                        submitButton.setEnabled(true);
                        JOptionPane.showMessageDialog(panel, "Request Failed：${connection.responseMessage}")
                    }
                } catch (ex: Exception) {
                    if (timer.isRunning) {
                        timer.stop()
                        submitButton.icon = IconLoader.getIcon("/icons/send.svg", javaClass)
                    }
                    jTextField.setEnabled(true);
                    buttonPanel.setEnabled(true)
                    submitButton.setEnabled(true);
                    println(ex.localizedMessage)
                    JOptionPane.showMessageDialog(panel, "Request Exception：${ex.localizedMessage}")
                }
                return null
            }

            override fun process(chunks: MutableList<String>?) {
                for (chunk in chunks!!) {
                    doc.insertString(doc.length, chunk, style);
                }
            }
        }
        worker.execute()
    }


}


