package settings

import com.intellij.icons.AllIcons
import com.intellij.ide.HelpTooltip
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.MessageType
import com.intellij.ui.EnumComboBoxModel
import com.intellij.ui.TitledSeparator
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPasswordField
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import java.awt.BorderLayout
import java.awt.FlowLayout
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.ItemEvent
import java.net.MalformedURLException
import java.net.URL
import javax.swing.Box
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.SwingUtilities

class CustomServiceForm {

    private var apiKeyField: JBPasswordField? = null
    private var urlField: JBTextField? = null
    private var tabbedPane: CustomServiceFormTabbedPane? = null
    private var testConnectionButton: JButton? = null
    private var templateHelpText: JBLabel? = null
    private var templateComboBox: ComboBox<CustomServiceTemplate>? = null

    fun CustomServiceForm(settings: CustomServiceSettingsState) {
        apiKeyField = JBPasswordField()
        apiKeyField.setColumns(30)
        apiKeyField.setText(CustomServiceCredentialManager.getInstance().getCredential())
        urlField = JBTextField(settings.getUrl(), 30)
        tabbedPane = CustomServiceFormTabbedPane(settings)
        testConnectionButton = JButton(
            CodeGPTBundle.get(
                "settingsConfigurable.service.custom.openai.testConnection.label"
            )
        )
        testConnectionButton.addActionListener(ActionListener { e: ActionEvent? ->
            testConnection(
                getCurrentState()
            )
        })
        templateHelpText = JBLabel(AllIcons.General.ContextHelp)
        templateComboBox = ComboBox<Any?>(
            EnumComboBoxModel<E>(CustomServiceTemplate::class.java)
        )
        templateComboBox!!.setSelectedItem(settings.getTemplate())
        templateComboBox!!.addItemListener { e: ItemEvent ->
            val template: CustomServiceTemplate = e.item as CustomServiceTemplate
            updateTemplateHelpTextTooltip(template)
            urlField.setText(template.getUrl())
            tabbedPane.setHeaders(template.getHeaders())
            tabbedPane.setBody(template.getBody())
        }
        updateTemplateHelpTextTooltip(settings.getTemplate())
    }

    fun getForm(): JPanel {
        val urlPanel = JPanel(BorderLayout(8, 0))
        urlPanel.add(urlField, BorderLayout.CENTER)
        urlPanel.add(testConnectionButton, BorderLayout.EAST)
        val templateComboBoxWrapper = JPanel(FlowLayout(FlowLayout.LEADING, 0, 0))
        templateComboBoxWrapper.add(templateComboBox)
        templateComboBoxWrapper.add(Box.createHorizontalStrut(8))
        templateComboBoxWrapper.add(templateHelpText)
        val form = FormBuilder.createFormBuilder()
            .addLabeledComponent(
                CodeGPTBundle.get("settingsConfigurable.service.custom.openai.presetTemplate.label"),
                templateComboBoxWrapper
            )
            .addLabeledComponent(
                CodeGPTBundle.get("settingsConfigurable.shared.apiKey.label"),
                apiKeyField!!
            )
            .addComponentToRightColumn(
                UIUtil.createComment("settingsConfigurable.service.custom.openai.apiKey.comment")
            )
            .addLabeledComponent(
                CodeGPTBundle.get("settingsConfigurable.service.custom.openai.url.label"),
                urlPanel
            )
            .addComponent(tabbedPane)
            .panel
        return FormBuilder.createFormBuilder()
            .addComponent(
                TitledSeparator(
                    CodeGPTBundle.get("settingsConfigurable.service.openai.configuration.title")
                )
            )
            .addComponent(withEmptyLeftBorder(form))
            .addComponentFillVertically(JPanel(), 0)
            .panel
    }

    fun getApiKey(): String? {
        val apiKey = String(apiKeyField!!.getPassword())
        return if (apiKey.isEmpty()) null else apiKey
    }

    fun getCurrentState(): CustomServiceSettingsState {
        val state = CustomServiceSettingsState()
        state.setUrl(urlField!!.getText())
        state.setTemplate(templateComboBox!!.getItem())
        state.setHeaders(tabbedPane.getHeaders())
        state.setBody(tabbedPane.getBody())
        return state
    }

    fun resetForm() {
        val state: Unit = CustomServiceSettings.getCurrentState()
        apiKeyField!!.setText(CustomServiceCredentialManager.getInstance().getCredential())
        urlField!!.setText(state.getUrl())
        templateComboBox!!.setSelectedItem(state.getTemplate())
        tabbedPane.setHeaders(state.getHeaders())
        tabbedPane.setBody(state.getBody())
    }

    private fun updateTemplateHelpTextTooltip(template: CustomServiceTemplate) {
        templateHelpText!!.setToolTipText(null)
        try {
            HelpTooltip()
                .setTitle(template.getName())
                .setBrowserLink(
                    CodeGPTBundle.get("settingsConfigurable.service.custom.openai.linkToDocs"),
                    URL(template.getDocsUrl())
                )
                .installOn(templateHelpText!!)
        } catch (e: MalformedURLException) {
            throw RuntimeException(e)
        }
    }

    private fun testConnection(customConfiguration: CustomServiceSettingsState) {
        val conversation = Conversation()
        val request: Unit = CompletionRequestProvider(conversation)
            .buildCustomOpenAIChatCompletionRequest(
                customConfiguration,
                CallParameters(conversation, Message("Hello!"))
            )
        CompletionRequestService.getInstance()
            .getCustomOpenAIChatCompletionAsync(request, TestConnectionEventListener())
    }


    internal class TestConnectionEventListener : CompletionEventListener<String?> {
        fun onMessage(value: String?, eventSource: EventSource) {
            if (value != null && !value.isEmpty()) {
                SwingUtilities.invokeLater {
                    OverlayUtil.showBalloon(
                        CodeGPTBundle.get("settingsConfigurable.service.custom.openai.connectionSuccess"),
                        MessageType.INFO,
                        testConnectionButton
                    )
                    eventSource.cancel()
                }
            }
        }

        fun onError(error: ErrorDetails, ex: Throwable?) {
            SwingUtilities.invokeLater {
                OverlayUtil.showBalloon(
                    """
                    ${CodeGPTBundle.get("settingsConfigurable.service.custom.openai.connectionFailed")}
                    
                    ${error.getMessage()}
                    """.trimIndent(),
                    MessageType.ERROR,
                    testConnectionButton
                )
            }
        }
    }

}