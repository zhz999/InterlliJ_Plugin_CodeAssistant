package code_assistant.window

import code_assistant.common.Icons
import code_assistant.common.Message
import code_assistant.statusbar.copilot.CopilotStatusBarWidget
import code_assistant.statusbar.copilot.CopilotStatusBarWidgetFactory
import code_assistant.tool.Bundle
import com.intellij.openapi.wm.WindowManager
import com.intellij.ui.DocumentAdapter
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBTextArea
import com.intellij.ui.content.Content
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import org.java_websocket.client.WebSocketClient
import java.awt.*
import java.awt.event.ActionListener
import javax.swing.*
import javax.swing.event.DocumentEvent

class Util {

    object UtilState {

        @JvmStatic
        @Volatile
        var RunningTimer: Timer? = null

    }

    companion object {

        private var counts = 0

        private const val textAreaRadius = 16

        val BACKGROUND_COLOR = JBColor.namedColor(
            "Editor.SearchField.background", UIUtil.getTextFieldBackground()
        )

        fun paintBorder(g: Graphics, textArea: JBTextArea) {
            val g2 = g.create() as Graphics2D
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            g2.color = JBUI.CurrentTheme.ActionButton.focusedBorder()
            if (textArea.isFocusOwner) {
                g2.stroke = BasicStroke(1.5f)
            }
            g2.drawRoundRect(0, 0, textArea.width - 1, textArea.height - 1, textAreaRadius, textAreaRadius)
        }

        fun addShiftEnterInputMap(textArea: JTextArea, onSubmit: AbstractAction?) {
            textArea.inputMap.put(KeyStroke.getKeyStroke("shift ENTER"), "insert-break")
            textArea.inputMap.put(KeyStroke.getKeyStroke("ENTER"), "text-submit")
            textArea.actionMap.put("text-submit", onSubmit)
        }


        fun getDocumentAdapter(sendBtnPanel: JPanel): DocumentAdapter {
            return object : DocumentAdapter() {
                override fun textChanged(event: DocumentEvent) {
                    sendBtnPanel.isEnabled = true
                }
            }
        }

        fun createIconButton(icon: Icon): JButton {
            val button = JButton(icon)
            button.border = BorderFactory.createEmptyBorder()
            button.isContentAreaFilled = false
            button.preferredSize = Dimension(icon.iconWidth, icon.iconHeight)
            return button
        }


        fun start(button: JButton) {
            if (UtilState.RunningTimer == null) {
                counts = 0
                UtilState.RunningTimer = Timer(300, ActionListener {
                    counts += 1
                    val flag = counts % 2
                    if (flag == 1) {
                        button.icon = Icons.Step1
                    } else {
                        button.icon = Icons.Step2
                    }
                })
                UtilState.RunningTimer?.start()
            } else {
                counts = 0
                if (UtilState.RunningTimer?.isRunning == false) {
                    UtilState.RunningTimer?.start()
                }
            }
        }

        fun stop(button: JButton) {
            counts = 0
            if (UtilState.RunningTimer?.isRunning == true) {
                UtilState.RunningTimer?.stop()
            }
            button.icon = Icons.Send
        }

    }


}