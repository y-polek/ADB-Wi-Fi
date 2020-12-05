package dev.polek.adbwifi.model

import dev.polek.adbwifi.model.Device.ConnectionType.*

data class Device(
    val id: String,
    val serialNumber: String,
    val name: String,
    val address: Address?,
    val androidVersion: String,
    val apiLevel: String,
    val isPinnedDevice: Boolean = false,
    var isConnected: Boolean = false
) {
    val uniqueId: String = "$serialNumber-$id"
    val connectionType: ConnectionType

    init {
        connectionType = if (isPinnedDevice) {
            NONE
        } else {
            val addressFromId = IP_ADDRESS_REGEX.matchEntire(id)?.groupValues?.getOrNull(1)
            if (addressFromId != null) WIFI else USB
        }
    }

    val isUsbDevice = connectionType == USB
    val isWifiDevice = connectionType == WIFI

    enum class ConnectionType {
        USB, WIFI, NONE
    }

    private companion object {
        private val IP_ADDRESS_REGEX = "(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})(:\\d{1,5})".toRegex()
    }
}
