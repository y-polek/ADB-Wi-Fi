package dev.polek.adbwifi.model

import dev.polek.adbwifi.model.Device.ConnectionType.*

data class Device(
    val id: String,
    val serialNumber: String,
    val name: String,
    var customName: String? = null,
    val address: Address?,
    val port: Int,
    val androidVersion: String,
    val apiLevel: String,
    val connectionType: ConnectionType,
    val isPinnedDevice: Boolean = false,
    var isConnected: Boolean = false
) {
    val uniqueId: String = "$serialNumber-$id"
    val isUsbDevice = connectionType == USB
    val isWifiDevice = connectionType == WIFI

    enum class ConnectionType {
        USB, WIFI, NONE
    }
}
