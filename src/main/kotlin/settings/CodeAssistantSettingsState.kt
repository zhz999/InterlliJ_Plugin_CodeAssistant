package settings
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.*;

@State(
    name = "CodeAssistantSettings",
    storages = [Storage("CodeAssistantSettings.xml")]
)
final class CodeAssistantSettingsState : PersistentStateComponent<CodeAssistantSettingsState> {
    var userId = "zhanghongzhong@bytedance.com"
    var ideaStatus = false
    var uri = "http://127.0.0.1:11434/api/chat"
    var model = "gemma:7b"

    @Nullable
    override fun getState(): CodeAssistantSettingsState {
        return this
    }

    override fun loadState(state: CodeAssistantSettingsState) {
        XmlSerializerUtil.copyBean(state, this)
    }

    companion object {
        fun getInstance(): CodeAssistantSettingsState {
            return ApplicationManager.getApplication().getService(CodeAssistantSettingsState::class.java)
        }
    }


}