package settings

import com.intellij.openapi.Disposable

class ServiceSelectionForm {
    private val customServiceForm: CustomServiceForm? = null
    fun ServiceSelectionForm(parentDisposable: Disposable?) {
        customServiceForm = CustomServiceForm(
            CustomServiceSettings.getCurrentState()
        )
    }

    fun getCustomConfigurationSettingsForm(): CustomServiceForm? {
        return customServiceForm
    }

    fun resetForms() {
        customServiceForm.resetForm()
    }
}