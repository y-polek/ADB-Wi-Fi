package dev.polek.adbwifi.model

data class Device(
    val id: String,
    val name: String,
    val address: String,
    val androidVersion: String,
    val apiLevel: String,
    val isConnected: Boolean = false
)
