package code_assistant.test


import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.JBInsets
import com.intellij.util.ui.JBUI
import java.awt.Dimension
import java.awt.Insets
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import javax.swing.*
import javax.swing.event.AncestorEvent
import javax.swing.event.AncestorListener

fun main() {
    val textField = JEditorPane()
    textField.contentType = "text/plain"
    textField.preferredSize = Dimension(100, 50)
    textField.margin = Insets(10, 8, 10, 8)
    textField.addKeyListener(object : KeyAdapter() {
        override fun keyPressed(e: KeyEvent?) {
            // 只监听回车键
            if (e?.keyCode != KeyEvent.VK_ENTER) return
            if (e.isShiftDown && (e.keyCode == KeyEvent.VK_ENTER)) {
                textField.document.insertString(textField.document.length, "\n", null)
            } else {
                e.consume()
                println("发送消息：${textField.text}")
            }
        }
    })
    JFrame().apply {
        defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        contentPane.add(textField)
        isVisible = true
        size = Dimension(400, 250)
    }


}




