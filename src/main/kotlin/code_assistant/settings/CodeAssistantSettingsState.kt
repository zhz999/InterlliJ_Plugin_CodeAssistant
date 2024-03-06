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
    var dorado = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJzZXJ2aWNlIjoiZG9yYWRvLWNvcGlsb3QiLCJ0ZW5hbnRJZCI6MCwiZXhwIjoxNzA5Njk0MzQ0LCJ1c2VyIjoiemhhbmdob25nemhvbmcifQ.FTSFDy4Vk5G6mpNlmGx84zwgvXzBlRm1MD_qKEaPODPFk4N_l-xceKm_1xnv51NexBZ5tbrF-VBagL3kN2XgonFMdRSLFYAoV9b1NmSme3i9znA24GC514rDoUlvMGIdRzSHQ0b8XMmS_5ubFq8Y11zy_Jax8DGtBJeo07WSWxZMSLNXwY0Xq4ruQiRbJrzSeZH9x7ZZeX3MBRbuZWrTizcc0yx6Ec0lcbplFYWxpIjSuKu2o_D9YV4pIA-CdmmWt-TrffNgtBHfUucUjJ2rUTNx3LVn9Y8i-CFYMO0A9qeGxfz8UNcnW2d6K1OwV0j0-TlfJP53QDnkWd-oJzIQLw"
    var enabled = true
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