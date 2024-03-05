package code_assistant.settings.configuration

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

@State(name = "CodeAssist_ConfigurationSettings", storages = [Storage("CodeAssist_ConfigurationSettings.xml")])
final class ConfigurationSettings : PersistentStateComponent<ConfigurationState> {
    private var state = ConfigurationState()
    override fun getState(): ConfigurationState {
        return state
    }

    override fun loadState(state: ConfigurationState) {
        this.state = state
    }

    companion object {

        fun getCurrentState(): ConfigurationState {
            return getInstance().getState()
        }

        fun getInstance(): ConfigurationSettings {
            return ApplicationManager.getApplication().getService(ConfigurationSettings::class.java)
        }

    }
}