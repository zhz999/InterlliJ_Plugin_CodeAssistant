package icons

import com.intellij.openapi.util.IconLoader

object Icons {
    @JvmField
    val Default = IconLoader.getIcon("/icons/codegpt-dark.svg", javaClass)
    @JvmField
    val DefaultSmall = IconLoader.getIcon("/icons/codegpt-small.svg", javaClass)
}