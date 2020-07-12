package dev.polek.adbwifi.model

data class Device(
        val id: String,
        val name: String,
        val address: String,
        val isConnected: Boolean = false)
