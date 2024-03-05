package code_assistant.tool

import code_assistant.common.ApplicationPath
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.ui.Messages
import java.awt.Desktop
import java.io.File
import java.io.IOException

class ApplicationWindow : DumbAwareAction() {

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.getData(PlatformDataKeys.PROJECT)
        try {
            // 指定应用程序的文件路径
            val applicationPath = ApplicationPath.MacOS
            if(applicationPath == null){
                Messages.showErrorDialog(project, "找不到应用文件", "打开插件失败")
                return
            }
            // 打开应用程序
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                Desktop.getDesktop().open(File(applicationPath))
            } else {
                Messages.showErrorDialog(project, "Desktop not supported or OPEN action not supported", "打开插件失败")
            }
        } catch (e: IOException) {
            Messages.showErrorDialog(project, e.stackTraceToString(), "打开插件失败")
        }
    }

}