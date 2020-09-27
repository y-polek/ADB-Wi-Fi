package dev.polek.adbwifi.model

data class PinnedDevice(
    val id: String,
    val serialNumber: String,
    val name: String,
    val address: String,
    val androidVersion: String,
    val apiLevel: String
)
