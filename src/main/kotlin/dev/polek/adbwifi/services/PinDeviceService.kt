package dev.polek.adbwifi.services

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil
import com.intellij.util.xmlb.annotations.OptionTag
import dev.polek.adbwifi.model.Device
import dev.polek.adbwifi.model.Device.ConnectionType.WIFI
import dev.polek.adbwifi.model.PinnedDevice
import dev.polek.adbwifi.model.PinnedDeviceListConverter

@State(
    name = "PinDeviceService",
    storages = [Storage("adbWifiPinnedDevices.xml")]
)
class PinDeviceService : PersistentStateComponent<PinDeviceService> {

    @OptionTag(converter = PinnedDeviceListConverter::class)
    var pinnedDevices: List<PinnedDevice> = listOf()

    fun addPreviouslyConnectedDevices(devices: List<Device>) {
        for (device in devices) {
            if (device.connectionType != WIFI) continue
            if (device.address.isNullOrBlank()) continue
            if (pinnedDevices.contains(device)) continue
            pinnedDevices = pinnedDevices.add(device)
        }
    }

    fun removePreviouslyConnectedDevice(device: Device) {
        pinnedDevices = pinnedDevices.remove(device)
    }

    override fun getState() = this

    override fun loadState(state: PinDeviceService) {
        XmlSerializerUtil.copyBean(state, this)
    }

    private companion object {

        private fun List<PinnedDevice>.contains(device: Device): Boolean {
            return this.find {
                it.androidId == device.androidId && it.address == device.address
            } != null
        }

        private fun List<PinnedDevice>.add(device: Device): List<PinnedDevice> {
            return this + PinnedDevice(
                id = device.id,
                androidId = device.androidId,
                name = device.name,
                address = device.address.orEmpty(),
                androidVersion = device.androidVersion,
                apiLevel = device.apiLevel
            )
        }

        private fun List<PinnedDevice>.remove(device: Device): List<PinnedDevice> {
            return this.filter {
                it.androidId != device.androidId || it.address != device.address
            }
        }
    }
}
