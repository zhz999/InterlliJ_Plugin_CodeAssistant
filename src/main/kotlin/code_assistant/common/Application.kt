package code_assistant.common


object ApplicationPath {
    @JvmField
    val MacOS = getResourceFilePath("/MacOS/DevAssistant.app")
}

fun getResourceFilePath(fileName: String): String? {
    val classLoader = Thread.currentThread().contextClassLoader
    val resourceUrl = classLoader.getResource(fileName)
    println("codeAssistant Path:${resourceUrl?.path}")
    return resourceUrl?.path
}



