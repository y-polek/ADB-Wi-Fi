package dev.polek.adbwifi.model

data class Address(
    val interfaceName: String,
    val ip: String
) {
    val isWlan: Boolean = interfaceName.contains("wlan", ignoreCase = true)
}
