package code_assistant.window

import code_assistant.common.DoradoCommon
import code_assistant.common.Icons
import code_assistant.common.Message
import code_assistant.settings.CodeAssistantSettingsState
import code_assistant.statusbar.copilot.CopilotStatusBarWidget
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.IconLoader
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.DocumentAdapter
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextArea
import com.intellij.ui.content.Content
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.content.ContentManagerEvent
import com.intellij.ui.content.ContentManagerListener
import com.intellij.util.ui.JBUI
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import org.json.JSONObject
import java.awt.*
import java.awt.datatransfer.Clipboard
import java.awt.datatransfer.StringSelection
import java.awt.event.*
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.text.*
import javax.swing.text.html.HTML
import javax.swing.text.html.HTMLDocument
import javax.swing.text.html.HTMLEditorKit
import javax.swing.text.html.parser.ParserDelegator

/**
 *
 * [官方文档](https://jetbrains.design/intellij/)
 *
 * [社区文档](https://www.ideaplugin.com/home.html)
 */
class ChatWindow : ToolWindowFactory {

    private val settings: CodeAssistantSettingsState = CodeAssistantSettingsState.getInstance()

    private var counts = 0

    object WsState {

        @JvmStatic
        @Volatile
        var isReconnect = false

        @JvmStatic
        @Volatile
        var reTimer: Timer? = null

        @JvmStatic
        @Volatile
        var wsToken: String = ""

        @JvmStatic
        @Volatile
        var wsClient: WebSocketClient? = null

        @JvmStatic
        @Volatile
        var wsTimer: Timer? = null

        @JvmStatic
        @Volatile
        var wsPingTimer: Timer? = null

        @JvmStatic
        @Volatile
        var doradoContent: Content? = null

        @JvmStatic
        var codeFlag = false
    }

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        try {

            // _Post.Init("https://data.bytedance.net/socket-dorado/copilot/token", null)

            val contentFactory = ContentFactory.getInstance()
            val component = toolWindow.component
            val size = SwingUtilities.getRoot(component).size
            val width = size.width
            val height = size.height
            val chatComponent = createToolWindowPanel(width, height, "Ollama", toolWindow)
            val chatContent = contentFactory.createContent(chatComponent, "Ollama", false)
            chatContent.icon = IconLoader.getIcon("/icons/ollama.svg", javaClass)
            toolWindow.contentManager.addContent(chatContent)
            if (settings.enabled) {
                val wsComponent = createToolWindowPanel(width, height, "Copilot", toolWindow)
                WsState.doradoContent = contentFactory.createContent(wsComponent, "Copilot", false)
                WsState.doradoContent!!.icon = IconLoader.getIcon("/icons/off-dorado.svg", javaClass)
                toolWindow.contentManager.addContent(WsState.doradoContent!!)
            }

            WsState.reTimer = Timer(3000, ActionListener {
                if (WsState.wsClient == null || WsState.wsClient?.isOpen == false) {
                    WsState.isReconnect = true
                    println("尝试重连")
                    val Copilot = toolWindow.contentManager.findContent("Copilot")
                    // 先移除销毁掉原来的Context
                    if (Copilot !== null) toolWindow.contentManager.removeContent(Copilot, true)
                    // 再重新创建新的Context
                    val wsComponent = createToolWindowPanel(width, height, "Copilot", toolWindow)
                    WsState.doradoContent = contentFactory.createContent(wsComponent, "Copilot", false)
                    WsState.doradoContent!!.icon = IconLoader.getIcon("/icons/off-dorado.svg", javaClass)
                    toolWindow.contentManager.addContent(WsState.doradoContent!!)
                } else {
                    WsState.isReconnect = false
                    println("连接成功")
                    WsState.reTimer?.stop()
                }
            })

            // 监听选择Context
            toolWindow.contentManager.addContentManagerListener(object : ContentManagerListener {
                override fun selectionChanged(event: ContentManagerEvent) {
                    if (settings.enabled) {
                        // if (event.content.displayName == "Copilot") {
                        if (WsState.wsClient == null || !WsState.wsClient!!.isOpen) {
                            if (!WsState.reTimer!!.isRunning) {
                                WsState.reTimer!!.start()
                            }
                        }
                        // }
                    }
                }
            })


        } catch (e: IOException) {
            Messages.showErrorDialog(project, e.stackTraceToString(), "打开插件失败")
        }
    }


    private fun createToolWindowPanel(
        width: Int, height: Int, displayName: String, toolWindow: ToolWindow
    ): JPanel {
        val panel = JPanel(BorderLayout())

        // 输出面板
        val textPane = JTextPane()
        val textPanePreferredSize = Dimension(width, (height * 0.8).toInt())
        textPane.name = "out"
        textPane.contentType = "text/html"
        textPane.editorKit = HTMLEditorKit()
        textPane.isEditable = false
        val font: Font = textPane.font
        textPane.font = font.deriveFont(10f);
        textPane.margin = JBUI.insets(8, 5);
        textPane.size = textPanePreferredSize
        textPane.preferredSize = textPanePreferredSize
        textPane.isDoubleBuffered = true
        val scrollPane = JBScrollPane(textPane)
        scrollPane.isDoubleBuffered = true
        scrollPane.border = BorderFactory.createEmptyBorder();
        panel.add(scrollPane, BorderLayout.CENTER)

        // 添加鼠标事件
        val menuItem = JMenuItem("Copy").apply {
            icon = Icons.Copy
            addActionListener {
                val copyTxt = textPane.selectedText ?: return@addActionListener
                val selection = StringSelection(copyTxt)
                val clipboard: Clipboard = Toolkit.getDefaultToolkit().systemClipboard
                clipboard.setContents(selection, null)
            }
        }
        val popupMenu = JPopupMenu()
        popupMenu.add(menuItem)
        textPane.addMouseListener(object : MouseAdapter() {
            override fun mousePressed(e: MouseEvent) {
                if (e.button == MouseEvent.BUTTON3) {
                    val copyTxt = textPane.selectedText ?: return
                    popupMenu.show(e.component, e.x, e.y)
                }
            }
        })


        // 输入面板
        val input = JBTextArea()
        val sendPanel = JPanel(FlowLayout(8, 10, 10))
        val inputPanel = JPanel(BorderLayout())
        val icon = Icons.Send
        val sendBtn: JButton = Util.createIconButton(icon)
        sendBtn.addActionListener {
            try {
                val text: String = input.text.replace("\n", "\n\n")
                if (displayName == "Ollama") {
                    submit(textPane, text, panel, sendPanel, input, sendBtn)
                } else {
                    sendMessage(sendPanel, input, textPane, sendBtn)
                }
            } finally {
                input.text = ""
            }
        }
        sendBtn.setCursor(Cursor(Cursor.HAND_CURSOR))
        sendBtn.setEnabled(false)
        inputPanel.isOpaque = false
        inputPanel.add(input, BorderLayout.CENTER)
        sendPanel.add(sendBtn)
        input.isDoubleBuffered = true
        input.font = input.font.deriveFont(14f);
        input.document.addDocumentListener(Util.getDocumentAdapter(sendPanel))
        input.isOpaque = false
        // input.setBackground(Util.BACKGROUND_COLOR)
        input.setLineWrap(true)
        input.setWrapStyleWord(true)
        input.getEmptyText().setText("Ask me anything...")
        input.setBorder(JBUI.Borders.empty(8, 4))
        Util.addShiftEnterInputMap(input, object : AbstractAction() {
            override fun actionPerformed(e: ActionEvent) {
                try {
                    val text: String = input.text.replace("\n", "\n\n")
                    if (displayName == "Ollama") {
                        submit(textPane, text, panel, sendPanel, input, sendBtn)
                    } else {
                        sendMessage(sendPanel, input, textPane, sendBtn)
                    }
                } finally {
                    input.text = ""
                }
            }
        })
        input.addFocusListener(object : FocusListener {
            override fun focusGained(e: FocusEvent) {
                if (panel.graphics !== null) Util.paintBorder(panel.graphics, input)
                if (input.text.isEmpty()) {
                    sendBtn.isEnabled = false
                }
            }

            override fun focusLost(e: FocusEvent) {
                if (panel.graphics !== null) Util.paintBorder(panel.graphics, input)
                if (input.text.isEmpty()) {
                    sendBtn.isEnabled = false
                }
            }
        })
        input.document.addDocumentListener(object : DocumentAdapter() {
            override fun textChanged(e: DocumentEvent) {
                sendBtn.isEnabled = true
            }
        })

        // ###############################################################
        inputPanel.add(sendPanel, BorderLayout.EAST)


        // 下拉选择 Panel
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
        modelList.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, JBColor.WHITE))
        toolPanel.add(modelList, BorderLayout.EAST)
        modelList.addActionListener {
            settings.model = modelList.getItemAt(modelList.getSelectedIndex())
        }
        val inputBoxPanel = JPanel()
        inputBoxPanel.setLayout(BorderLayout())//上下布局
        if (displayName == "Ollama") {
            inputBoxPanel.add(toolPanel, BorderLayout.NORTH)//上
        }
        inputBoxPanel.add(JSeparator(), BorderLayout.CENTER)//上
        inputBoxPanel.add(inputPanel, BorderLayout.SOUTH)//下
        panel.add(inputBoxPanel, BorderLayout.SOUTH)

        if (displayName == "Copilot") {
            WsState.wsTimer = Timer(500, ActionListener {
                counts += 1
                val flag = counts % 2
                if (flag == 1) {
                    // sendBtn.setEnabled(true);
                } else {
                    // sendBtn.setEnabled(false);
                }
            })
            connectWs(
                textPane,
                sendBtn,
                sendPanel,
                input,
                WsState.wsTimer!!,
                scrollPane,
                toolWindow,
            )
        }

        return panel
    }

    private fun sendMessage(
        buttonPanel: JPanel,
        input: JBTextArea,
        outPanel: JTextPane,
        button: JButton
    ) {
        val text: String = input.text.replace("\n", "\n\n")
        val doc = outPanel.styledDocument
        val style: Style = doc.addStyle("Issue", null)
        StyleConstants.setFontSize(style, 12);
        val htmlKit = HTMLEditorKit()
        val htmlContent = """<html><body>
                        <div style="border:'1px solid #dee0e3';border-radius:'10px';padding:'8px';background:'#dee0e3';">${text}</div>
                        <br></body></html>""".trimIndent()
        try {
            htmlKit.insertHTML(doc as HTMLDocument, doc.length, htmlContent, 0, 0, null)
        } catch (ex: BadLocationException) {
            Message.Error("写入数据出错:${ex.message}")
        }
        //buttonPanel.setEnabled(false)
        //input.setEnabled(false);
        if (!WsState.wsTimer?.isRunning!!) {
            WsState.wsTimer!!.start()
        }
        Util.start(button)
        val mes = parseMessage(text)
        WsState.wsClient?.send(mes)
    }

    companion object {

        fun reCopilot() {
            val settings = CodeAssistantSettingsState.getInstance()
            if (settings.enabled) {
                if (WsState.wsClient == null || !WsState.wsClient!!.isOpen) {
                    if (!WsState.reTimer!!.isRunning) {
                        WsState.reTimer!!.start()
                    }
                } else {
                    Message.Warn("Copilot Connected!")
                }
            }
        }

        /**
         *
         * Copilot Data PareCovert
         */
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

        /**
         *
         * ollama send message
         */
        @JvmStatic
        fun send(
            panel: JPanel, buttonPanel: JPanel, jTextField: JComponent, message: String, outPanel: JTextPane
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
            //buttonPanel.setEnabled(false)
            //jTextField.setEnabled(false);
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


    /**
     *
     * Set Connection Copilot Status
     */
    private fun setStatus(toolWindow: ToolWindow, status: String) {
        // var ico = "/icons/app-icon-off.svg"
        if (status == "Success") {
            // ico = "/icons/app-icon-online.svg"
            if (!WsState.isReconnect) {
                Message.Info("Copilot Is Ready OK!")
            }
            CopilotStatusBarWidget.WidgetStates.icon = Icons.Running
        } else {
            if (!WsState.isReconnect) {
                Message.Error("Copilot Is Ready Failed : $status")
            }
            CopilotStatusBarWidget.WidgetStates.icon = Icons.Disabled
        }
        //val icon: Icon = IconLoader.getIcon(ico, javaClass)
        //val application: Application = ApplicationManager.getApplication()
        //application.invokeLater { toolWindow.setIcon(icon) }
    }


    /**
     *
     * Connection Copilot
     */
    private fun connectWs(
        textPane: JTextPane,
        submitButton: JButton,
        buttonPanel: JPanel,
        jTextField: Component,
        timer: Timer,
        scrollPane: JBScrollPane,
        toolWindow: ToolWindow
    ) {

        if (WsState.wsPingTimer !== null) {
            WsState.wsPingTimer?.stop()
            WsState.wsPingTimer = null
        }

        // 创建一个定时器，用于发送ping
        WsState.wsPingTimer = Timer(3000, ActionListener {
            WsState.wsClient?.sendPing()
        })

        val token = DoradoCommon.getToken()
        if (token.isEmpty()) {
            Message.Error("获取Token失败")
            return
        } else {
            WsState.wsToken = token
        }

        // 创建一个样式化文本的样式
        val styleContext = StyleContext()
        val codeStyle = styleContext.addStyle("CodeStyle", null)
        StyleConstants.setFontFamily(codeStyle, "Courier New")
        StyleConstants.setFontSize(codeStyle, 10)
        StyleConstants.setForeground(codeStyle, JBColor.decode("#865B03"))


        WsState.wsClient = object :
            WebSocketClient(URI("wss://data.bytedance.net/socket-dorado/copilot/v1/socket?token=" + WsState.wsToken)) {
            override fun onOpen(handshakedata: ServerHandshake?) {
                println("WebSocketClient Connect Success")
                WsState.doradoContent!!.icon = IconLoader.getIcon("/icons/dorado.svg", javaClass)
                setStatus(toolWindow, "Success")
                WsState.wsClient = this
                // 启动或停止定时器
                if (!WsState.wsPingTimer!!.isRunning) {
                    WsState.wsPingTimer!!.start()
                }
                if (WsState.isReconnect) Message.Info("Copilot Success!")
            }

            override fun onMessage(message: String?) {
                val doc = textPane.styledDocument
                val verticalScrollBar = scrollPane.verticalScrollBar
                verticalScrollBar.value = verticalScrollBar.maximum
                val style: Style = doc.addStyle("Solution", null)
                StyleConstants.setFontSize(style, 10);
                // println(message)
                val content = JSONObject(message).getJSONObject("result").getJSONObject("content")
                if (content.has("type") && content.getString("type") == "answer_end") {
                    doc.insertString(doc.length, "\n\n\n\n\n", style);
                    println("=========== answer_end")
                    jTextField.setEnabled(true);
                    buttonPanel.setEnabled(true)
                    submitButton.setEnabled(true);
                    if (timer.isRunning) {
                        timer.stop()
                    }
                    Util.stop(submitButton)
                }

                if (content.has("type") && content.getString("type") == "answer_start") {
                    // 开始接收数据
                    // WsState.codeArray = arrayOf("")
                }

                if (content.has("part")) {
                    val txt = content.getString("part")
                    if (txt.contains("```") || txt.contains("``")) {
                        WsState.codeFlag = !WsState.codeFlag
                        if (!WsState.codeFlag) {
                            // 结束时
                            try {
                                val strTmp = txt.split("```")
                                doc.insertString(doc.length, strTmp[0] + "```", codeStyle);
                                doc.insertString(doc.length, strTmp[1], style);
                            } catch (_: Exception) {

                            }
                        } else {
                            // 开始时
                            try {
                                val strTmp = txt.split("```")
                                doc.insertString(doc.length, strTmp[0], style);
                                doc.insertString(doc.length, "```" + strTmp[1], codeStyle);
                            } catch (_: Exception) {

                            }
                        }
                    } else {
                        if (WsState.codeFlag) {
                            doc.insertString(doc.length, txt, codeStyle);
                        } else {
                            doc.insertString(doc.length, txt, style);
                        }
                    }
                }
            }

            override fun onClose(code: Int, reason: String?, remote: Boolean) {
                WsState.doradoContent!!.icon = IconLoader.getIcon("/icons/dorado.svg", javaClass)
                setStatus(toolWindow, "Closed")
                println("WebSocketClient Connect onClose:$reason")
                if (timer.isRunning) {
                    timer.stop()
                }
                // 启动或停止定时器
                if (WsState.wsPingTimer!!.isRunning) {
                    WsState.wsPingTimer!!.stop()
                }
                jTextField.setEnabled(true);
                buttonPanel.setEnabled(true)
                submitButton.setEnabled(true);
            }

            override fun onError(ex: java.lang.Exception?) {
                println("WebSocketClient Connect onError:${ex?.stackTraceToString()}")
                WsState.doradoContent!!.icon = IconLoader.getIcon("/icons/dorado.svg", javaClass)
                setStatus(toolWindow, "Exception")
                if (timer.isRunning) {
                    timer.stop()
                }
                // 启动或停止定时器
                if (WsState.wsPingTimer!!.isRunning) {
                    WsState.wsPingTimer!!.stop()
                }
                jTextField.setEnabled(true);
                buttonPanel.setEnabled(true)
                submitButton.setEnabled(true);
            }
        }
        (WsState.wsClient as WebSocketClient).connect()
    }


    /**
     *
     * https://github.com/ollama/ollama/blob/main/docs/api.md
     */
    fun submit(
        textPane: JTextPane,
        inputText: String,
        panel: JPanel,
        sendPanel: JPanel,
        inputPanel: JComponent,
        sendBtn: JButton
    ) {
        //sendPanel.setEnabled(false)
        //inputPanel.setEnabled(false);
        var counts = 0
        // 创建一个定时器，用于模拟加载过程
        val timer = Timer(500, ActionListener {
            counts += 1
            // 模拟加载过程
            //submitButton.isVisible = !submitButton.isVisible
            val flag = counts % 2
            if (flag == 1) {
                // sendBtn.setEnabled(true);
            } else {
                // sendBtn.setEnabled(false);
            }
        })

        // 启动或停止定时器
        if (!timer.isRunning) {
            timer.start()
        }

        // 创建并执行后台任务
        val doc = textPane.styledDocument
        val style: Style = doc.addStyle("default", null)

        // 问题
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
                        val inputStream = connection.inputStream
                        val reader = BufferedReader(InputStreamReader(inputStream, "utf-8"))
                        var line: String?
                        while (reader.readLine().also { line = it } != null) {
                            val gson = Gson()
                            val jsonObject = gson.fromJson(line, JsonObject::class.java)
                            val context = jsonObject.get("message").asJsonObject.get("content").asString
                            publish(context)
                            if (jsonObject.get("done").asBoolean) {
                                inputPanel.setEnabled(true);
                                sendBtn.setEnabled(true)
                                publish("\r\n\n\n\n\n")
                                if (timer.isRunning) {
                                    timer.stop()
                                }
                            }
                        }
                        reader.close()
                    } else {
                        if (timer.isRunning) {
                            timer.stop()
                        }
                        inputPanel.setEnabled(true);
                        sendBtn.setEnabled(true)
                        JOptionPane.showMessageDialog(panel, "Request Failed：${connection.responseMessage}")
                    }
                } catch (ex: Exception) {
                    if (timer.isRunning) {
                        timer.stop()
                    }
                    inputPanel.setEnabled(true);
                    sendBtn.setEnabled(true)
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






