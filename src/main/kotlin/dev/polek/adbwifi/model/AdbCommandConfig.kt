package dev.polek.adbwifi.model

data class AdbCommandConfig(
    val id: String,
    val name: String,
    val command: String,
    val iconId: String = "",
    val isEnabled: Boolean,
    val order: Int
)
