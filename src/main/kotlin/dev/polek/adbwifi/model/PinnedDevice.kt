package dev.polek.adbwifi.model

import dev.polek.adbwifi.model.Device.ConnectionType.NONE

data class PinnedDevice(
    val id: String,
    val serialNumber: String,
    val name: String,
    val address: String,
    val port: Int,
    val androidVersion: String,
    val apiLevel: String
) {
    companion object {
        fun PinnedDevice.toDevice() = Device(
            id = this.id,
            serialNumber = this.serialNumber,
            name = this.name,
            address = Address("", this.address),
            port = this.port,
            androidVersion = this.androidVersion,
            apiLevel = this.apiLevel,
            connectionType = NONE,
            isPinnedDevice = true
        )
    }
}
