package settings


class GeneralSettingsState {


    private var displayName = ""
    private var selectedService = ""


    fun getDisplayName(): String {
        if (displayName.isEmpty()) {
            val systemUserName = System.getProperty("user.name")
            return if (systemUserName == null || systemUserName.isEmpty()) {
                "User"
            } else systemUserName
        }
        return displayName as String
    }

    fun setDisplayName(displayName: String?) {
        if (displayName != null) {
            this.displayName = displayName
        }
    }

    fun getSelectedService(): String {
        return selectedService
    }

    fun setSelectedService(selectedService:String) {
        this.selectedService = selectedService
    }
}
