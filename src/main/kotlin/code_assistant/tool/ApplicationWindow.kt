package code_assistant.tool

import code_assistant.common.ApplicationPath
import code_assistant.common.Message
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.project.DumbAwareAction
import java.awt.Desktop
import java.io.File
import java.io.IOException

class ApplicationWindow : DumbAwareAction() {

    override fun actionPerformed(event: AnActionEvent) {
        // val project = event.getData(PlatformDataKeys.PROJECT)
        try {
            // 指定应用程序的文件路径
            val applicationPath = ApplicationPath.MacOS
            if(applicationPath == null){
                Message.Error("找不到应用文件")
                return
            }
            // 打开应用程序
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                Desktop.getDesktop().open(File(applicationPath))
            } else {
                Message.Error("Desktop not supported or OPEN action not supported")
            }
        } catch (e: IOException) {
            Message.Error(e.stackTraceToString())
        }
    }

}