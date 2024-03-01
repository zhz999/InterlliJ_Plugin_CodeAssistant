package settings

import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import javax.swing.JComponent
import javax.swing.JPanel
import org.jetbrains.annotations.*


class AppSettingsComponent {
    private var myMainPanel: JPanel? = null
    private val myUserNameText = JBTextField()
    private val myIdeaUserStatus = JBCheckBox("Do you use IntelliJ IDEA? ")

    fun AppSettingsComponent() {
        myMainPanel = FormBuilder.createFormBuilder()
            .addLabeledComponent(JBLabel("Enter user name: "), myUserNameText, 1, false)
            .addComponent(myIdeaUserStatus, 1)
            .addComponentFillVertically(JPanel(), 0)
            .getPanel()
    }

    fun getPanel(): JPanel? {
        return myMainPanel
    }

    fun getPreferredFocusedComponent(): JComponent {
        return myUserNameText
    }

    @NotNull
    fun getUserNameText(): String {
        return myUserNameText.getText()
    }

    fun setUserNameText(@NotNull newText: String?) {
        myUserNameText.setText(newText)
    }

    fun getIdeaUserStatus(): Boolean {
        return myIdeaUserStatus.isSelected
    }

    fun setIdeaUserStatus(newStatus: Boolean) {
        myIdeaUserStatus.setSelected(newStatus)
    }
}