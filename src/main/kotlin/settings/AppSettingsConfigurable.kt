package settings

import com.intellij.openapi.options.Configurable
import org.jetbrains.annotations.*
import javax.swing.JComponent


class AppSettingsConfigurable: Configurable {
    private var mySettingsComponent: AppSettingsComponent? = null

    // A default constructor with no arguments is required because this implementation
    // is registered in an applicationConfigurable EP

    // A default constructor with no arguments is required because this implementation
    // is registered in an applicationConfigurable EP
    @Nls(capitalization = Nls.Capitalization.Title)
    override fun getDisplayName(): String {
        return "SDK: Application Settings Example"
    }

    override fun getPreferredFocusedComponent(): JComponent {
        return mySettingsComponent!!.getPreferredFocusedComponent()
    }

    @Nullable
    override fun createComponent(): JComponent? {
        mySettingsComponent = AppSettingsComponent()
        return mySettingsComponent!!.getPanel()
    }

    override fun isModified(): Boolean {
        val settings: AppSettingsState = AppSettingsState.getInstance()
        var modified = mySettingsComponent!!.getUserNameText() != settings.userId
        modified = modified or (mySettingsComponent!!.getIdeaUserStatus() != settings.ideaStatus)
        return modified
    }

    override fun apply() {
        val settings: AppSettingsState = AppSettingsState.getInstance()
        settings.userId = mySettingsComponent!!.getUserNameText()
        settings.ideaStatus = mySettingsComponent!!.getIdeaUserStatus()
    }

    override fun reset() {
        val settings: AppSettingsState = AppSettingsState.getInstance()
        mySettingsComponent!!.setUserNameText(settings.userId)
        mySettingsComponent!!.setIdeaUserStatus(settings.ideaStatus)
    }

    override fun disposeUIResources() {
        mySettingsComponent = null
    }
}