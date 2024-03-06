package code_assistant.common

import com.intellij.ui.JBColor
import com.intellij.util.ui.JBUI
import java.awt.*
import java.awt.geom.RoundRectangle2D
import javax.swing.border.Border

/**
 *  创建圆角边框
 */
 class RoundBorder(private val radius: Int) : Border {

    override fun paintBorder(c: Component?, g: Graphics, x: Int, y: Int, width: Int, height: Int) {
        val g2d = g.create() as Graphics2D
        g2d.color= Color.decode("#dee0e3")
        g2d.draw(
            RoundRectangle2D.Double(
                x.toDouble(),
                y.toDouble(),
                (width - 1).toDouble(),
                (height - 1).toDouble(),
                radius.toDouble(),
                radius.toDouble()
            )
        )
        g2d.dispose()
    }

    override fun getBorderInsets(c: Component?): Insets {
        val value = radius / 2
        return JBUI.insets(value, value, value, value)
    }

     override fun isBorderOpaque(): Boolean {
         return false;
     }



 }