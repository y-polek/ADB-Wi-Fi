package dev.polek.adbwifi.ui.presenter

import com.intellij.openapi.components.service
import dev.polek.adbwifi.model.CommandHistory
import dev.polek.adbwifi.model.LogEntry
import dev.polek.adbwifi.model.PinnedDevice
import dev.polek.adbwifi.services.*
import dev.polek.adbwifi.ui.model.DeviceViewModel
import dev.polek.adbwifi.ui.model.DeviceViewModel.Companion.toViewModel
import dev.polek.adbwifi.ui.view.ToolWindowView
import dev.polek.adbwifi.utils.copyToClipboard

class ToolWindowPresenter {

    private var view: ToolWindowView? = null
    private val adbService by lazy { service<AdbService>() }
    private val scrcpyService by lazy { service<ScrcpyService>() }
    private val logService by lazy { service<LogService>() }
    private val propertiesService by lazy { service<PropertiesService>() }
    private val pinDeviceService by lazy { service<PinDeviceService>() }

    private var isViewOpen: Boolean = false
    private var isAdbValid: Boolean = true
    private var devices: List<DeviceViewModel> = emptyList()
    private var pinnedDevices: List<DeviceViewModel> = pinDeviceService.pinnedDevices.map { it.toViewModel() }

    fun attach(view: ToolWindowView) {
        this.view = view
        view.showEmptyMessage()
        subscribeToDeviceList()
        subscribeToLogEvents()
        subscribeToAdbLocationChanges()
    }

    fun detach() {
        unsubscribeFromDeviceList()
        unsubscribeFromLogEvents()
        unsubscribeFromAdbLocationChanges()
        view = null
    }

    fun onViewOpen() {
        isViewOpen = true
        if (isAdbValid) {
            subscribeToDeviceList()
        }
    }

    fun onViewClosed() {
        isViewOpen = false
        unsubscribeFromDeviceList()
    }

    fun onConnectButtonClicked(device: DeviceViewModel) {
        devices.findById(device.id)?.isInProgress = true
        pinnedDevices.findById(device.id)?.isInProgress = true
        view?.showDevices(devices)
        view?.showPinnedDevices(pinnedDevices)

        adbService.connect(device.device)
    }

    fun onDisconnectButtonClicked(device: DeviceViewModel) {
        devices.findById(device.id)?.isInProgress = true
        pinnedDevices.findById(device.id)?.isInProgress = true
        view?.showDevices(devices)
        view?.showPinnedDevices(pinnedDevices)

        adbService.disconnect(device.device)
    }

    fun onShareScreenButtonClicked(device: DeviceViewModel) {
        if (scrcpyService.isScrcpyValid()) {
            scrcpyService.share(device.device)
        } else {
            view?.showInvalidScrcpyLocationError()
        }
    }

    fun onRemoveDeviceButtonClicked(device: DeviceViewModel) {
        pinDeviceService.removePreviouslyConnectedDevice(device.device)
        pinnedDevices = pinDeviceService.pinnedDevices.toViewModel()
        view?.showPinnedDevices(pinnedDevices)
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
                pinnedDevices = pinDeviceService.pinnedDevices.toViewModel()

                if (devices.isEmpty() && pinnedDevices.isEmpty()) {
                    view?.showEmptyMessage()
                } else {
                    view?.showDevices(devices)
                    view?.showPinnedDevices(pinnedDevices)
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

    private fun subscribeToAdbLocationChanges() {
        propertiesService.adbLocationListener = { isValid ->
            isAdbValid = isValid
            if (!isValid) {
                unsubscribeFromDeviceList()
                devices = emptyList()
                view?.showInvalidAdbLocationError()
            } else {
                if (devices.isEmpty() && pinnedDevices.isEmpty()) {
                    view?.showEmptyMessage()
                } else {
                    view?.showDevices(devices)
                    view?.showPinnedDevices(pinnedDevices)
                }
                if (isViewOpen) {
                    subscribeToDeviceList()
                }
            }
        }
    }

    private fun unsubscribeFromAdbLocationChanges() {
        propertiesService.adbLocationListener = null
    }

    private fun List<PinnedDevice>.toViewModel(): List<DeviceViewModel> {
        return this.asSequence()
            .filter { pinnedDevice ->
                devices.find { device ->
                    device.androidId == pinnedDevice.androidId && device.address == pinnedDevice.address
                } == null
            }
            .sortedBy { it.name }
            .map { it.toViewModel() }
            .toList()
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
