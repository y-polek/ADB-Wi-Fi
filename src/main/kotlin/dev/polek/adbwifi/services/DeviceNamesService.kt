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

    @OptionTag
    private val deviceNames = mutableMapOf<String/*Serial number*/, String/*Name*/>()

    fun setName(serialNumber: String, name: String) {
        deviceNames[serialNumber] = name
    }

    fun removeName(serialNumber: String) {
        deviceNames.remove(serialNumber)
    }

    fun findName(serialNumber: String): String? {
        return deviceNames[serialNumber]
    }

    override fun getState() = this

    override fun loadState(state: DeviceNamesService) {
        XmlSerializerUtil.copyBean(state, this)
    }
}