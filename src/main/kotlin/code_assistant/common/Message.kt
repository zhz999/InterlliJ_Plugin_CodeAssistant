package code_assistant.common

import code_assistant.tool.Bundle
import com.intellij.notification.*

class Message {

    companion object {
        fun Error(error: String) {
            Notifications.Bus.notify(
                Notification(
                    "CodeAssist Notification Group",
                    Bundle.message("notification.group.title","Error"),
                    error,
                    NotificationType.ERROR
                ).setIcon(Icons.MessageError)
            )
        }

        fun Warn(warn: String) {
            Notifications.Bus.notify(
                Notification(
                    "CodeAssist Notification Group",
                    Bundle.message("notification.group.title","Warning"),
                    warn,
                    NotificationType.WARNING
                ).setIcon(Icons.MessageWarn)
            )
        }


        fun Info(info: String) {
            Notifications.Bus.notify(
                Notification(
                    "CodeAssist Notification Group",
                    Bundle.message("notification.group.title",""),
                    info,
                    NotificationType.INFORMATION
                ).setIcon(Icons.MessageInfo)
            )
        }
    }


}