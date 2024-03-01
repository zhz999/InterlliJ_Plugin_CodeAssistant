package settings

import com.intellij.openapi.Disposable
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.util.Disposer
import javax.swing.JComponent

class GeneralSettingsConfigurable : Configurable {

    private var parentDisposable: Disposable? = null
    private var component: GeneralSettingsComponent? = null

    override fun getPreferredFocusedComponent(): JComponent {
        return component.getPreferredFocusedComponent()
    }

    override fun createComponent(): JComponent? {
        val settings: Unit = GeneralSettings.getInstance()
        parentDisposable = Disposer.newDisposable()
        component = GeneralSettingsComponent(parentDisposable, settings)
        return component.getPanel()
    }

    override fun isModified(): Boolean {
        // 判断配置是否被修改
        val settings: Unit = GeneralSettings.getCurrentState()
        val serviceSelectionForm: Unit = component.getServiceSelectionForm()
        return (!component.getDisplayName()
            .equals(settings.getDisplayName()) || component.getSelectedService() !== settings.getSelectedService() || OpenAISettings.getInstance()
            .isModified(serviceSelectionForm.getOpenAISettingsForm())
                || CustomServiceSettings.getInstance()
            .isModified(serviceSelectionForm.getCustomConfigurationSettingsForm())
                || AzureSettings.getInstance().isModified(serviceSelectionForm.getAzureSettingsForm())
                || YouSettings.getInstance().isModified(serviceSelectionForm.getYouSettingsForm())
                || LlamaSettings.getInstance().isModified(serviceSelectionForm.getLlamaSettingsForm()))
    }

    override fun apply() {
        val settings: Unit = GeneralSettings.getCurrentState()
        settings.setDisplayName(component.getDisplayName())
        settings.setSelectedService(component.getSelectedService())

        val serviceSelectionForm: Unit = component.getServiceSelectionForm()
        val openAISettingsForm: Unit = serviceSelectionForm.getOpenAISettingsForm()
        applyOpenAISettings(openAISettingsForm)
        applyCustomOpenAISettings(serviceSelectionForm.getCustomConfigurationSettingsForm())
        applyAzureSettings(serviceSelectionForm.getAzureSettingsForm())
        applyYouSettings(serviceSelectionForm.getYouSettingsForm())
        applyLlamaSettings(serviceSelectionForm.getLlamaSettingsForm())

        val serviceChanged = component.getSelectedService() !== settings.getSelectedService()
        val modelChanged: Boolean = !OpenAISettings.getCurrentState().getModel()
            .equals(openAISettingsForm.getModel())
        if (serviceChanged || modelChanged) {
            resetActiveTab()
            if (serviceChanged) {
                TelemetryAction.SETTINGS_CHANGED.createActionMessage()
                    .property("service", component.getSelectedService().getCode().toLowerCase())
                    .send()
            }
        }
    }

    private fun applyCustomOpenAISettings(form: CustomServiceForm) {
        CustomServiceCredentialManager.getInstance().setCredential(form.getApiKey())
        CustomServiceSettings.getInstance().loadState(form.getCurrentState())
    }

    override fun reset() {
        val settings: Unit = GeneralSettings.getCurrentState()
        component.setDisplayName(settings.getDisplayName())
        component.setSelectedService(settings.getSelectedService())
        component.getServiceSelectionForm().resetForms()
    }

    override fun disposeUIResources() {
        if (parentDisposable != null) {
            Disposer.dispose(parentDisposable!!)
        }
        component = null
    }

    override fun getDisplayName(): String {
        // 返回显示名称
        return "开发助手: Settings"
    }

    private fun resetActiveTab() {
        ConversationsState.getInstance().setCurrentConversation(null)
        val project: Unit = ApplicationUtil.findCurrentProject()
            ?: throw RuntimeException("Could not find current project.")
        project.getService(StandardChatToolWindowContentManager::class.java).resetAll()
    }
}