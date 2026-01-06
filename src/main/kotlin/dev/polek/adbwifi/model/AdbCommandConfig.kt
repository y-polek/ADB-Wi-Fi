package dev.polek.adbwifi.model

data class AdbCommandConfig(
    val name: String,
    val command: String,
    val iconId: String = "",
    val isEnabled: Boolean,
    val order: Int,
    val requiresConfirmation: Boolean = false
) {
    val requiresPackage: Boolean get() = command.contains("{package}")
}
