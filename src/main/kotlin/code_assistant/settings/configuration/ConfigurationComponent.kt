package code_assistant.settings.configuration

import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.components.JBLabel
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.FormBuilder
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UI
import code_assistant.editor.EditorActionsUtil
import java.awt.Dimension
import java.util.LinkedHashMap
import javax.swing.BorderFactory
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.table.DefaultTableModel

class ConfigurationComponent(configuration: ConfigurationState) {
    val panel: JPanel
    private val table: JBTable

    init {
        table = JBTable(
            DefaultTableModel(
                EditorActionsUtil.toArray(configuration.getTableData()),
                arrayOf("Action", "Prompt")
            )
        )
        table.columnModel.getColumn(0).setPreferredWidth(60)
        table.columnModel.getColumn(1).setPreferredWidth(240)
        table.emptyText.setText("No actions configured")
        val tablePanel = createTablePanel()
        tablePanel.setBorder(BorderFactory.createTitledBorder("Editor Actions"))
        panel = FormBuilder.createFormBuilder()
            .addComponent(tablePanel)
            .addVerticalGap(4)
            .addComponentFillVertically(JPanel(), 0)
            .panel
    }

    val getCurrentFormState: ConfigurationState
        get() {
            val state = ConfigurationState()
            state.setTableData(tableData)
            return state
        }

    fun resetForm() {
        val configuration: ConfigurationState = ConfigurationSettings.getCurrentState()
        tableData = configuration.getTableData()
    }

    private var tableData: Map<String, String>
        get() {
            val model = model
            val data: MutableMap<String, String> = LinkedHashMap()
            for (count in 0 until model.rowCount) {
                data[model.getValueAt(count, 0).toString()] = model.getValueAt(count, 1).toString()
            }
            return data
        }
        set(tableData) {
            val model = model
            model.setNumRows(0)
            tableData.forEach { (action: String, prompt: String) ->
                model.addRow(
                    arrayOf<Any>(action, prompt)
                )
            }
        }

    private fun createTablePanel(): JPanel {
        return ToolbarDecorator.createDecorator(table)
            .setPreferredSize(Dimension(table.getPreferredSize().width, 300))
            .setAddAction { model.addRow(arrayOf<Any>("", "")) }
            .setRemoveAction { model.removeRow(table.selectedRow) }
            .disableUpAction()
            .disableDownAction()
            .createPanel()
    }

    private val model: DefaultTableModel
        get() = table.model as DefaultTableModel

}