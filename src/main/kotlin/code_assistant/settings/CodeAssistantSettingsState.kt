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
    var dorado = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJzZXJ2aWNlIjoiZG9yYWRvLWNvcGlsb3QiLCJ0ZW5hbnRJZCI6MCwiZXhwIjoxNzA5NjIxMTM4LCJ1c2VyIjoiemhhbmdob25nemhvbmcifQ.rPOZyYZI41LoWtG14J0evvwVn_81-QPaf5gH2Yy3P4_hKDaBVMJGSrlE0fHnVNCzwQR7oEl5B19Ooq-2mFXrsOa8spwGcfdSuyXFRDhJ398R-pITv93tUoo5HEum3KJsI7A9BrglAYk7EUOvldG-_Bov-oyVICibl_fAKfZmhgM-QPPHD1-Z1D8kECfXsLmI63qQoLgWKS-RPJJHEHJJn0B5BLwc9z-IEOhD8k1PByctS3ZmtX16jtwLpGeY4VI5KnLDDhSuBwRflU8jKz23BvgTPNi-OKcCAC4zquBUaXt8Td-vf1GhhVdrlh3zKUqh_B-Ir_vyUEGdRlBqIaZ54w"
    var enabledDorado = true

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