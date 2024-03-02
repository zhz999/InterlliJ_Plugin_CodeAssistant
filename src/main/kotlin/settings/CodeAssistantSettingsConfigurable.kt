package settings

import com.intellij.openapi.options.Configurable
import org.jetbrains.annotations.*
import javax.swing.JComponent


class CodeAssistantSettingsConfigurable: Configurable {
    private var mySettingsComponent: CodeAssistantSettingsComponent? = null

    @Nls(capitalization = Nls.Capitalization.Title)
    override fun getDisplayName(): String {
        return "开发助手: Settings"
    }

    override fun getPreferredFocusedComponent(): JComponent {
        return mySettingsComponent!!.getPreferredFocusedComponent()
    }

    @Nullable
    override fun createComponent(): JComponent? {
        mySettingsComponent = CodeAssistantSettingsComponent()
        return mySettingsComponent!!.getPanel()
    }

    override fun isModified(): Boolean {
        val settings: CodeAssistantSettingsState = CodeAssistantSettingsState.getInstance()
        var modified = mySettingsComponent!!.getUserNameText() != settings.userId
        modified = modified or (mySettingsComponent!!.getIdeaUserStatus() != settings.ideaStatus)
        modified = modified or (mySettingsComponent!!.getModel() != settings.model)
        modified = modified or (mySettingsComponent!!.getUri() != settings.uri)
        return modified
    }

    override fun apply() {
        val settings: CodeAssistantSettingsState = CodeAssistantSettingsState.getInstance()
        settings.userId = mySettingsComponent!!.getUserNameText()
        settings.ideaStatus = mySettingsComponent!!.getIdeaUserStatus()
        settings.model = mySettingsComponent!!.getModel()
        settings.uri = mySettingsComponent!!.getUri()
    }

    override fun reset() {
        val settings: CodeAssistantSettingsState = CodeAssistantSettingsState.getInstance()
        mySettingsComponent!!.setUserNameText(settings.userId)
        mySettingsComponent!!.setIdeaUserStatus(settings.ideaStatus)
        mySettingsComponent!!.setModel(settings.model)
        mySettingsComponent!!.setUri(settings.uri)
    }

    override fun disposeUIResources() {
        mySettingsComponent = null
    }
}