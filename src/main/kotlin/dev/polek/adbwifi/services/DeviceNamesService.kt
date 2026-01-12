package dev.polek.adbwifi.services

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil
import com.intellij.util.xmlb.annotations.OptionTag

@State(
    name = "DeviceNamesService",
    storages = [Storage("deviceNames.xml")]
)
class DeviceNamesService : PersistentStateComponent<DeviceNamesService> {

    // Legacy: maps serial number to name (kept for backwards compatibility)
    @OptionTag
    private val deviceNames = mutableMapOf<String, String>()

    // Maps unique ID (serial:address) to custom name
    @OptionTag
    private val deviceNamesByUniqueId = mutableMapOf<String, String>()

    /**
     * Sets a custom name for a specific device by unique ID.
     */
    fun setNameByUniqueId(uniqueId: String, name: String) {
        deviceNamesByUniqueId[uniqueId] = name
    }

    /**
     * Removes all custom names for a device (both by unique ID and legacy serial-based).
     */
    fun removeAllNames(serialNumber: String, uniqueId: String) {
        deviceNamesByUniqueId.remove(uniqueId)
        deviceNames.remove(serialNumber)
    }

    /**
     * Finds a custom name for a device.
     * First checks for a name by unique ID, then falls back to legacy serial-based name.
     */
    fun findName(serialNumber: String, uniqueId: String): String? {
        return deviceNamesByUniqueId[uniqueId] ?: deviceNames[serialNumber]
    }

    override fun getState() = this

    override fun loadState(state: DeviceNamesService) {
        XmlSerializerUtil.copyBean(state, this)
    }
}
