package dev.polek.adbwifi.ui.presenter

import com.intellij.openapi.components.service
import dev.polek.adbwifi.model.CommandHistory
import dev.polek.adbwifi.model.Device
import dev.polek.adbwifi.model.LogEntry
import dev.polek.adbwifi.model.PinnedDevice
import dev.polek.adbwifi.services.*
import dev.polek.adbwifi.ui.model.DeviceViewModel
import dev.polek.adbwifi.ui.model.DeviceViewModel.Companion.toViewModel
import dev.polek.adbwifi.ui.view.ToolWindowView
import dev.polek.adbwifi.utils.BasePresenter
import dev.polek.adbwifi.utils.copyToClipboard
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ToolWindowPresenter : BasePresenter<ToolWindowView>() {

    private val adbService by lazy { service<AdbService>() }
    private val scrcpyService by lazy { service<ScrcpyService>() }
    private val logService by lazy { service<LogService>() }
    private val propertiesService by lazy { service<PropertiesService>() }
    private val pinDeviceService by lazy { service<PinDeviceService>() }
    private val deviceNamesService by lazy { service<DeviceNamesService>() }

    private var isViewOpen: Boolean = false
    private var isAdbValid: Boolean = true
    private var devices: List<DeviceViewModel> = emptyList()
    private var pinnedDevices: List<DeviceViewModel> = pinDeviceService.pinnedDevices.toViewModel()

    private var connectingDevices = mutableSetOf<Pair<String/*Device's unique ID*/, String/*IP address*/>>()
    private var deviceCollectionJob: Job? = null

    override fun attach(view: ToolWindowView) {
        super.attach(view)
        view.showEmptyMessage()
        subscribeToDeviceList()
        subscribeToLogEvents()
        subscribeToAdbLocationChanges()
        subscribeToScrcpyEnabledState()
    }

    override fun detach() {
        unsubscribeFromDeviceList()
        unsubscribeFromLogEvents()
        unsubscribeFromAdbLocationChanges()
        unsubscribeFromScrcpyEnabledState()
        super.detach()
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
        executeDeviceAction(device) { adbService.connect(it) }
    }

    fun onDisconnectButtonClicked(device: DeviceViewModel) {
        executeDeviceAction(device) { adbService.disconnect(it) }
    }

    private fun executeDeviceAction(device: DeviceViewModel, action: suspend (Device) -> Unit) {
        connectingDevices.add(device)
        updateDeviceLists()

        launch {
            withContext(IO) {
                action(device.device)
            }
            onDevicesUpdated(adbService.devices.value)
        }.invokeOnCompletion {
            connectingDevices.remove(device)
            updateDeviceLists()
        }
    }

    fun onShareScreenButtonClicked(device: DeviceViewModel) {
        if (scrcpyService.isScrcpyValid()) {
            launch(IO) {
                val result = scrcpyService.share(device.device)
                if (result.isError) {
                    view?.showScrcpyError(result.output)
                }
            }
        } else {
            view?.showInvalidScrcpyLocationError()
        }
    }

    fun onRemoveDeviceButtonClicked(device: DeviceViewModel) {
        if (propertiesService.confirmDeviceRemoval) {
            view?.showRemoveDeviceConfirmation(device)
        } else {
            removeDevice(device)
        }
    }

    fun onRemoveDeviceConfirmed(device: DeviceViewModel, doNotAskAgain: Boolean) {
        propertiesService.confirmDeviceRemoval = !doNotAskAgain

        removeDevice(device)
    }

    private fun removeDevice(device: DeviceViewModel) {
        pinDeviceService.removePreviouslyConnectedDevice(device.device)
        pinnedDevices = pinDeviceService.pinnedDevices.toViewModel()
        updateDeviceLists()
    }

    fun onRenameDeviceClicked(device: DeviceViewModel) {
        view?.showRenameDeviceDialog(device)
        updateDeviceNames()
    }

    private fun updateDeviceNames() {
        devices = devices.map {
            it.device.toViewModel(customName = deviceNamesService.findName(it.serialNumber))
        }
        pinnedDevices = pinDeviceService.pinnedDevices.toViewModel()
        updateDeviceLists()
    }

    fun onCopyDeviceIdClicked(device: DeviceViewModel) {
        copyToClipboard(device.device.id)
    }

    fun onCopyDeviceAddressClicked(device: DeviceViewModel) {
        val address = device.device.address ?: return
        copyToClipboard(address.ip)
    }

    private fun onDevicesUpdated(model: List<Device>) {
        devices = model.map {
            it.toViewModel(customName = deviceNamesService.findName(it.serialNumber))
        }
        pinnedDevices = pinDeviceService.pinnedDevices.toViewModel()

        updateDeviceLists()
    }

    private fun updateDeviceLists() {
        val isScrcpyEnabled = propertiesService.scrcpyEnabled

        devices.forEach {
            it.isInProgress = connectingDevices.contains(it)
            it.isShareScreenButtonVisible = isScrcpyEnabled
        }
        pinnedDevices.forEach {
            it.isInProgress = connectingDevices.contains(it)
        }

        if (!isAdbValid) {
            view?.showInvalidAdbLocationError()
        } else if (devices.isEmpty() && pinnedDevices.isEmpty()) {
            view?.showEmptyMessage()
        } else {
            view?.showDevices(devices)
            view?.showPinnedDevices(pinnedDevices)
        }
    }

    private fun subscribeToDeviceList() {
        if (deviceCollectionJob != null) {
            // Already subscribed
            return
        }
        deviceCollectionJob = launch {
            adbService.devices.collect { devices ->
                onDevicesUpdated(devices)
            }
        }
    }

    private fun unsubscribeFromDeviceList() {
        deviceCollectionJob?.cancel()
        deviceCollectionJob = null
    }

    private fun subscribeToLogEvents() {
        logService.logVisibilityListener = ::updateLogVisibility
    }

    private fun unsubscribeFromLogEvents() {
        logService.logVisibilityListener = null
    }

    private fun subscribeToScrcpyEnabledState() {
        propertiesService.scrcpyEnabledListener = {
            updateDeviceLists()
        }
    }

    private fun unsubscribeFromScrcpyEnabledState() {
        propertiesService.scrcpyEnabledListener = null
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
                updateDeviceLists()
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
                    device.serialNumber == pinnedDevice.serialNumber && device.address == pinnedDevice.address
                } == null
            }
            .sortedBy { it.name }
            .map {
                it.toViewModel(customName = deviceNamesService.findName(it.serialNumber))
            }
            .toList()
    }

    private companion object {

        private fun MutableSet<Pair<String/*Unique ID*/, String/*IP address*/>>.add(device: DeviceViewModel) {
            this.add(device.uniqueId to device.address.orEmpty())
        }

        private fun MutableSet<Pair<String/*Unique ID*/, String/*IP address*/>>.remove(device: DeviceViewModel) {
            this.remove(device.uniqueId to device.address.orEmpty())
        }

        private fun MutableSet<Pair<String/*Unique ID*/, String/*IP address*/>>.contains(
            device: DeviceViewModel
        ): Boolean {
            return this.find { (uniqueId, address) -> uniqueId == device.uniqueId && address == device.address } != null
        }
    }
}
