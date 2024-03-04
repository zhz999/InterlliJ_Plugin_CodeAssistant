package settings.configuration

import com.intellij.openapi.Disposable
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.util.Disposer
import org.jetbrains.annotations.Nls
import javax.swing.JComponent


class ConfigurationConfigurable : Configurable {
    private var parentDisposable: Disposable? = null
    private var component: ConfigurationComponent? = null

    @Nls(capitalization = Nls.Capitalization.Title)
    override fun getDisplayName(): String {
        return "开发助手: Editor Configuration"
    }

    override fun createComponent(): JComponent? {
        parentDisposable = Disposer.newDisposable()
        component = ConfigurationComponent(
            parentDisposable!!,
            ConfigurationSettings.getCurrentState()
        )
        return component!!.panel
    }

    override fun isModified(): Boolean {
        return component!!.getCurrentFormState != ConfigurationSettings.getCurrentState()
    }

    override fun apply() {
        ConfigurationSettings.getInstance().loadState(component!!.getCurrentFormState)
    }

    override fun reset() {
        component!!.resetForm()
    }

    override fun disposeUIResources() {
        if (parentDisposable != null) {
            Disposer.dispose(parentDisposable!!)
        }
        component = null
    }
}

