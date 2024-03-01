package settings

import com.intellij.openapi.Disposable
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import java.awt.CardLayout
import java.awt.Component
import java.awt.Container
import java.awt.Dimension
import java.awt.event.ItemEvent
import java.util.*
import java.util.stream.Collectors
import javax.swing.DefaultComboBoxModel
import javax.swing.JComponent
import javax.swing.JPanel

class GeneralSettingsComponent( parentDisposable:Disposable, settings:GeneralSettings) {

    private var mainPanel: JPanel? = null
    private var displayNameField: JBTextField? = null
    private var serviceComboBox: ComboBox<ServiceType>? = null
    private var serviceSelectionForm: ServiceSelectionForm? = null

    fun GeneralSettingsComponent(parentDisposable: Disposable?, settings: GeneralSettings) {
        displayNameField = JBTextField(settings.getState().getDisplayName(), 20)
        serviceSelectionForm = ServiceSelectionForm(parentDisposable)
        val cardLayout: ee.carlrobert.codegpt.settings.GeneralSettingsComponent.DynamicCardLayout =
            ee.carlrobert.codegpt.settings.GeneralSettingsComponent.DynamicCardLayout()
        val cards: JPanel = JPanel(cardLayout)
        cards.add(serviceSelectionForm.getOpenAISettingsForm().getForm(), OPENAI.getCode())
        cards.add(
            serviceSelectionForm.getCustomConfigurationSettingsForm().getForm(),
            CUSTOM_OPENAI.getCode()
        )
        cards.add(serviceSelectionForm.getAzureSettingsForm().getForm(), AZURE.getCode())
        cards.add(serviceSelectionForm.getYouSettingsForm(), YOU.getCode())
        cards.add(serviceSelectionForm.getLlamaSettingsForm(), LLAMA_CPP.getCode())
        val serviceComboBoxModel: DefaultComboBoxModel<ServiceType> = DefaultComboBoxModel<ServiceType>()
        serviceComboBoxModel.addAll(
            Arrays.stream(ServiceType.values())
                .collect(Collectors.toList())
        )
        serviceComboBox = ComboBox<Any?>(serviceComboBoxModel)
        serviceComboBox!!.setSelectedItem(OPENAI)
        serviceComboBox!!.setPreferredSize(displayNameField.getPreferredSize())
        serviceComboBox!!.addItemListener { e: ItemEvent ->
            cardLayout.show(
                cards,
                (e.item as ServiceType).getCode()
            )
        }
        mainPanel = FormBuilder.createFormBuilder()
            .addLabeledComponent(
                CodeGPTBundle.get("settingsConfigurable.displayName.label"),
                displayNameField
            )
            .addLabeledComponent(
                CodeGPTBundle.get("settingsConfigurable.service.label"),
                serviceComboBox!!
            )
            .addComponent(cards)
            .addComponentFillVertically(JPanel(), 0)
            .panel
    }


    fun getSelectedService(): ServiceType {
        return serviceComboBox!!.getItem()
    }

    fun setSelectedService(serviceType: ServiceType?) {
        serviceComboBox!!.setSelectedItem(serviceType)
    }

    fun getPanel(): JPanel {
        return mainPanel!!
    }

    fun getPreferredFocusedComponent(): JComponent {
        return displayNameField!!
    }

    fun getServiceSelectionForm(): ServiceSelectionForm {
        return serviceSelectionForm
    }

    fun getDisplayName(): String {
        return displayNameField!!.getText()
    }

    fun setDisplayName(displayName: String?) {
        displayNameField!!.setText(displayName)
    }


    internal class DynamicCardLayout : CardLayout() {
        override fun preferredLayoutSize(parent: Container): Dimension {
            val current = findVisibleComponent(parent)
            if (current != null) {
                val insets = parent.insets
                val preferredSize = current.preferredSize
                preferredSize.width += insets.left + insets.right
                preferredSize.height += insets.top + insets.bottom
                return preferredSize
            }
            return super.preferredLayoutSize(parent)
        }

        private fun findVisibleComponent(parent: Container): Component? {
            for (comp in parent.components) {
                if (comp.isVisible) {
                    return comp
                }
            }
            return null
        }
    }

}