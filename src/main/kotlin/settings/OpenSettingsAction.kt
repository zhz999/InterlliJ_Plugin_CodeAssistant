package settings

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.options.ShowSettingsUtil


class OpenSettingsAction : AnAction(
    "Open Setting",
    "Open 开发助手 setting",
    AllIcons.General.Settings
) {
    override fun actionPerformed(e: AnActionEvent) {
        ShowSettingsUtil.getInstance()
            .showSettingsDialog(e.project, GeneralSettingsConfigurable::class.java)
    }
}

