package dev.polek.adbwifi.ui.presenter

import com.intellij.openapi.components.service
import dev.polek.adbwifi.model.CommandHistory
import dev.polek.adbwifi.model.LogEntry
import dev.polek.adbwifi.services.AdbService
import dev.polek.adbwifi.services.LogService
import dev.polek.adbwifi.ui.model.DeviceViewModel
import dev.polek.adbwifi.ui.model.DeviceViewModel.Companion.toViewModel
import dev.polek.adbwifi.ui.view.ToolWindowView
import dev.polek.adbwifi.utils.copyToClipboard

class ToolWindowPresenter {

    private var view: ToolWindowView? = null
    private val adbService by lazy { service<AdbService>() }
    private val logService by lazy { service<LogService>() }

    private var devices: List<DeviceViewModel> = emptyList()

    fun attach(view: ToolWindowView) {
        this.view = view
        view.showEmptyMessage()
        subscribeToDeviceList()
        subscribeToLogEvents()
    }

    fun detach() {
        unsubscribeFromDeviceList()
        unsubscribeFromLogEvents()
        view = null
    }

    fun onViewOpen() {
        subscribeToDeviceList()
    }

    fun onViewClosed() {
        unsubscribeFromDeviceList()
    }

    fun onConnectButtonClicked(device: DeviceViewModel) {
        devices.findById(device.id)?.isInProgress = true
        view?.showDevices(devices)

        adbService.connect(device.device)
    }

    fun onDisconnectButtonClicked(device: DeviceViewModel) {
        devices.findById(device.id)?.isInProgress = true
        view?.showDevices(devices)

        adbService.disconnect(device.device)
    }

    fun onPinButtonClicked(device: DeviceViewModel) {
        TODO("Not implemented")
    }

    fun onCopyDeviceIdClicked(device: DeviceViewModel) {
        copyToClipboard(device.device.id)
    }

    fun onCopyDeviceAddressClicked(device: DeviceViewModel) {
        val address = device.device.address ?: return
        copyToClipboard(address)
    }

    private fun subscribeToDeviceList() {
        if (adbService.deviceListListener != null) {
            // Already subscribed
            return
        }
        adbService.deviceListListener = { model ->
            val oldDevices = devices
            devices = model.map { it.toViewModel() }
            if (!oldDevices.contentDeepEquals(devices)) {
                if (devices.isEmpty()) {
                    view?.showEmptyMessage()
                } else {
                    view?.showDevices(devices)
                }
            }
        }
    }

    private fun unsubscribeFromDeviceList() {
        if (adbService.deviceListListener == null) {
            // Already unsubscribed
            return
        }
        adbService.deviceListListener = null
    }

    private fun subscribeToLogEvents() {
        logService.logVisibilityListener = ::updateLogVisibility
    }

    private fun unsubscribeFromLogEvents() {
        logService.logVisibilityListener = null
    }

    private fun updateLogVisibility(isLogVisible: Boolean) {
        if (isLogVisible) {
            view?.openLog()
            logService.commandHistory.listener = object : CommandHistory.Listener {
                override fun onLogEntriesModified(entries: List<LogEntry>) {
                    view?.setLogEntries(entries)
                }
            }
        } else {
            view?.closeLog()
            logService.commandHistory.listener = null
        }
    }

    private companion object {
        private fun List<DeviceViewModel>.contentDeepEquals(other: List<DeviceViewModel>): Boolean {
            return this.toTypedArray().contentDeepEquals(other.toTypedArray())
        }

        private fun List<DeviceViewModel>.findById(id: String): DeviceViewModel? {
            return this.find { it.id == id }
        }
    }
}
