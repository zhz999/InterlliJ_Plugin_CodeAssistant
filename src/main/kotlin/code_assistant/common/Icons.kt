package code_assistant.common
import com.intellij.openapi.util.IconLoader


/**
 *
 * [颜色选择器](https://universe.bytedance.net/develop/react/color-picker)
 *
 * [官方文档颜色](https://jetbrains.design/intellij/principles/icons/#export-icons)
 */

object Icons {


    @JvmField
    val Done = IconLoader.getIcon("/icons/done.svg", javaClass)
    @JvmField
    val Step1 = IconLoader.getIcon("/icons/step1.svg", javaClass)
    @JvmField
    val Step2 = IconLoader.getIcon("/icons/step2.svg", javaClass)

    @JvmField
    val Disabled = IconLoader.getIcon("/icons/running-3.svg", javaClass)
    @JvmField
    val Running = IconLoader.getIcon("/icons/running-2.svg", javaClass)

    @JvmField
    val MessageInfo = IconLoader.getIcon("/icons/message-info-icon.svg", javaClass)
    @JvmField
    val MessageWarn = IconLoader.getIcon("/icons/message-warn-icon.svg", javaClass)
    @JvmField
    val MessageError = IconLoader.getIcon("/icons/message-error-icon.svg", javaClass)
    @JvmField
    val DefaultSmall = IconLoader.getIcon("/icons/app-icon-off.svg", javaClass)
    @JvmField
    val Send = IconLoader.getIcon("/icons/send.svg", javaClass)
}





