package dev.polek.adbwifi.ui.view

import dev.polek.adbwifi.model.LogEntry
import dev.polek.adbwifi.ui.model.DeviceViewModel

interface ToolWindowView {

    fun showDevices(devices: List<DeviceViewModel>, pinnedDevices: List<DeviceViewModel>)

    fun showEmptyMessage()

    fun showInvalidAdbLocationError()

    fun showInvalidScrcpyLocationError()

    fun openLog()

    fun closeLog()

    fun setLogEntries(entries: List<LogEntry>)
}
