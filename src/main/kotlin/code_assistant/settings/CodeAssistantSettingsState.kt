package code_assistant.settings

import com.intellij.ide.BrowserUtil
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil
import org.jetbrains.annotations.Nullable
import java.net.URISyntaxException
import javax.swing.event.HyperlinkEvent

@State(name = "CodeAssist_Settings", storages = [Storage("CodeAssist_Settings.xml")])
final class CodeAssistantSettingsState : PersistentStateComponent<CodeAssistantSettingsState> {
    var userId = "zhanghongzhong@bytedance.com"
    var uri = "http://127.0.0.1:11434/api/chat"
    var model = "gemma:7b"
    var token = "这里使用的是 https://data.bytedance.net/dorado 的 Token"
    var sessionId = ""
    var parentMessageId = ""
    var language = ""
    var enabled = false
    var gpt = "Ollama"

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

        fun handleHyperlinkClicked(event: HyperlinkEvent) {
            val url = event.url
            if (HyperlinkEvent.EventType.ACTIVATED == event.eventType && url != null) {
                try {
                    BrowserUtil.browse(url.toURI())
                } catch (e: URISyntaxException) {
                    throw RuntimeException(e)
                }
            }
        }
    }

}