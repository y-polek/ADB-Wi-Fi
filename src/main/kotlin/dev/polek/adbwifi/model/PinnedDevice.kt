package dev.polek.adbwifi.model

data class PinnedDevice(
    val androidId: String = "",
    val name: String = "",
    val address: String? = null,
    val androidVersion: String = "",
    val apiLevel: String = ""
)
