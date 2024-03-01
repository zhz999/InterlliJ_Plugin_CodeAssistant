package settings
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.*;

@State(
    name = "org.intellij.sdk.settings.AppSettingsState",
    storages = arrayOf(Storage("SdkSettingsPlugin.xml"))
)
final class AppSettingsState : PersistentStateComponent<AppSettingsState> {
    var userId = "John Q. Public"
    var ideaStatus = false

    fun getInstance(): AppSettingsState {
        return ApplicationManager.getApplication().getService(AppSettingsState::class.java)
    }

    @Nullable
    override fun getState(): AppSettingsState {
        return this
    }

    override fun loadState(state: AppSettingsState) {
        XmlSerializerUtil.copyBean(state!!, this)
    }

    companion object {
        fun getInstance(): AppSettingsState {
            return ApplicationManager.getApplication().getService(AppSettingsState::class.java)
        }
    }


}