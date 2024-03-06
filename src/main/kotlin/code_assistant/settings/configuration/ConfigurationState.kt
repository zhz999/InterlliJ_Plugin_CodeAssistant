package code_assistant.settings.configuration

import code_assistant.editor.EditorActionsUtil
import java.util.*

class ConfigurationState {

    private var tableData: Map<String, String> = EditorActionsUtil.defaultActions

    fun getTableData(): Map<String, String> {
        return tableData
    }

    fun setTableData(tableData: Map<String, String>) {
        this.tableData = tableData
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || javaClass != other.javaClass) {
            return false
        }
        val that = other as ConfigurationState
        return tableData == that.tableData // && useGPT == that.useGPT
    }

    override fun hashCode(): Int {
        return Objects.hash(
            tableData // , useGPT
        )
    }


}