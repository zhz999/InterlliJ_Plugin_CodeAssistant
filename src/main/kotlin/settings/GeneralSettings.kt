package settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage


@State(name = "CodeGPT_GeneralSettings_210", storages = [Storage("CodeGPT_GeneralSettings_210.xml")])
class GeneralSettings : PersistentStateComponent<GeneralSettingsState> {
    private var state: GeneralSettingsState = GeneralSettingsState()
    override fun getState(): GeneralSettingsState {
        return state
    }

    override fun loadState(state: GeneralSettingsState) {
        this.state = state
    }
}
