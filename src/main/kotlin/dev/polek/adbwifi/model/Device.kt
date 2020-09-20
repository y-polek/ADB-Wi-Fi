package dev.polek.adbwifi.model

data class Device(
    val id: String,
    val androidId: String,
    val name: String,
    val address: String?,
    val androidVersion: String,
    val apiLevel: String,
    val connectionType: ConnectionType,
    var isConnected: Boolean = false
) {
    val isUsbDevice = connectionType == ConnectionType.USB
    val isWifiDevice = connectionType == ConnectionType.WIFI

    enum class ConnectionType {
        USB, WIFI, NONE
    }
}
