package code_assistant.common


class ApplicationPath {

    companion object {
        private fun getResourceFilePath(fileName: String): String? {
            val classLoader = Thread.currentThread().contextClassLoader
            val resourceUrl = classLoader.getResource(fileName)
            println("codeAssistant Path:${resourceUrl?.path}")
            return resourceUrl?.path
        }

        @JvmField
        val MacOS: String? = getResourceFilePath("/MacOS/DevAssistant.app")
    }


}





