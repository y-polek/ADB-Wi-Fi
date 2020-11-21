package dev.polek.adbwifi.model

import dev.polek.adbwifi.model.Device.ConnectionType.*

data class Device(
    val id: String,
    val serialNumber: String,
    val name: String,
    val addresses: List<Address>,
    val androidVersion: String,
    val apiLevel: String,
    val isPinnedDevice: Boolean = false,
    var isConnected: Boolean = false
) {
    val uniqueId: String = "$serialNumber-$id"
    var selectedAddress: Address? = addresses.firstOrNull()

    val connectionType: ConnectionType
    val address: String?

    init {
        if (isPinnedDevice) {
            connectionType = NONE
            address = selectedAddress?.ip
        } else {
            val addressFromId = IP_ADDRESS_REGEX.matchEntire(id)?.groupValues?.getOrNull(1)
            connectionType = if (addressFromId != null) WIFI else USB
            address = if (connectionType == WIFI) addressFromId else selectedAddress?.ip
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
