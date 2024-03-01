package com.zhz.bytedance.development_assistant_zhz

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.ui.Messages
import java.awt.Desktop
import java.io.File
import java.io.IOException

class DevAction : DumbAwareAction() {

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.getData(PlatformDataKeys.PROJECT)
        try {
            // 获取桌面实例
            val desktop: Desktop = Desktop.getDesktop()
            // 指定应用程序的文件路径
            val applicationPath = "/Users/bytedance/IdeaProjects/GPT/my-new-app/out/DevAssistant-darwin-arm64/DevAssistant.app"
            // 打开应用程序
            desktop.open(File(applicationPath))
        } catch (e: IOException) {
            Messages.showErrorDialog(project, e.stackTraceToString(), "打开插件失败")
        }
    }

}