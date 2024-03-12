package code_assistant.editor

import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.util.IconLoader


/**
 * 注册 Editor 工具栏
 */


class EditorPopupMenu : ActionGroup() {
    override fun getChildren(e: AnActionEvent?): Array<AnAction> {

        EditorActionsUtil.refreshActions()

        val assistAction =
            CustomAnnoaction("Open Assistant", "Open Assistant", IconLoader.getIcon("/icons/app-icon-off.svg", javaClass))

        assistAction.registerCustomShortcutSet(CustomShortcutSet.fromString("control shift Z"), null)

        val assistSetAction =
            CustomAnnoaction("Open Settings", "Open Settings", IconLoader.getIcon("/icons/app-icon-off.svg", javaClass))


        // 创建子菜单项
        return arrayOf(
            assistAction,
            assistSetAction,
            Separator.getInstance(),
        )
    }
}


