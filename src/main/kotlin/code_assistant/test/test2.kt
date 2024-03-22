package code_assistant.test

import java.awt.BorderLayout
import javax.swing.JDialog
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JProgressBar

//object Main {
//    @JvmStatic
    fun main(args: Array<JLabel>) {
        val parentFrame = JFrame()
        parentFrame.setSize(500, 150)
        val jl = JLabel()
        jl.text = "Count : 0"
        parentFrame.add(BorderLayout.CENTER, jl)
        parentFrame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        parentFrame.isVisible = true
        val dlg = JDialog(parentFrame, "Progress Dialog", true)
        val dpb = JProgressBar(0, 500)
        dlg.add(BorderLayout.CENTER, dpb)
        dlg.add(BorderLayout.NORTH, JLabel("Progress..."))
        dlg.defaultCloseOperation = JDialog.DO_NOTHING_ON_CLOSE
        dlg.setSize(300, 75)
        dlg.setLocationRelativeTo(parentFrame)
        val t = Thread { dlg.isVisible = true }
        t.start()
        for (i in 0..500) {
            jl.text = "Count : $i"
            dpb.value = i
            if (dpb.value == 500) {
                dlg.isVisible = false
                System.exit(0)
            }
            try {
                Thread.sleep(25)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
        dlg.isVisible = true
    }
//}


