package com.zhz.bytedance.development_assistant_zhz

import com.intellij.ui.JBColor
import com.intellij.util.ui.JBUI
import java.awt.Component
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Insets
import java.awt.geom.RoundRectangle2D
import javax.swing.border.Border


 class RoundBorder(private val radius: Int) : Border {

    override fun paintBorder(c: Component?, g: Graphics, x: Int, y: Int, width: Int, height: Int) {
        val g2d = g.create() as Graphics2D
        g2d.color= JBColor.BLUE
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