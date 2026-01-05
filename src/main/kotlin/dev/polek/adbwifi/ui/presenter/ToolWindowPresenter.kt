package dev.polek.adbwifi.ui.presenter

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.MessageDialogBuilder
import com.intellij.openapi.ui.Messages
import dev.polek.adbwifi.PluginBundle
import dev.polek.adbwifi.model.AdbCommandConfig
import dev.polek.adbwifi.model.Device
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

class ToolWindowPresenter(private val project: Project) : BasePresenter<ToolWindowView>() {

    private val adbService by lazy { service<AdbService>() }
    private val adbCommandsService by lazy { service<AdbCommandsService>() }
    private val scrcpyService by lazy { service<ScrcpyService>() }
    private val logService by lazy { service<LogService>() }
    private val propertiesService by lazy { service<PropertiesService>() }
    private val pinDeviceService by lazy { service<PinDeviceService>() }
    private val deviceNamesService by lazy { service<DeviceNamesService>() }
    private val packageService by lazy { project.service<PackageService>() }

    private var isViewOpen: Boolean = false
    private var isAdbValid: Boolean = true
    private var devices: List<DeviceViewModel> = emptyList()
    private var pinnedDevices: List<DeviceViewModel> = pinDeviceService.pinnedDevices.toViewModel()

    private var connectingDevices = mutableSetOf<Pair<String/*Device's unique ID*/, String/*IP address*/>>()
    private val selectedPackages = mutableMapOf<String, String>()
    private var deviceCollectionJob: Job? = null
    private var logVisibilityJob: Job? = null
    private var logEntriesJob: Job? = null
    private var adbLocationJob: Job? = null
    private var scrcpyEnabledJob: Job? = null

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

    fun getSelectedPackage(device: DeviceViewModel): String? {
        return selectedPackages[device.id] ?: packageService.getPackageName()
    }

    fun setSelectedPackage(device: DeviceViewModel, packageName: String?) {
        if (packageName == null) {
            selectedPackages.remove(device.id)
        } else {
            selectedPackages[device.id] = packageName
        }
    }

    fun getInstalledPackages(device: DeviceViewModel): List<String> {
        return adbService.listPackages(device.id)
    }

    fun onAdbCommandClicked(device: DeviceViewModel, command: AdbCommandConfig) {
        val packageName = if (command.requiresPackage) {
            getSelectedPackage(device) ?: return
        } else {
            ""
        }

        if (command.requiresConfirmation) {
            val commandText = command.command.replace("{package}", packageName)
            val fullCommand = commandText.lines()
                .filter { it.isNotBlank() }
                .joinToString("\n") { "adb -s ${device.id} ${it.trim()}" }

            val result = MessageDialogBuilder.yesNo(
                PluginBundle.message("adbCommandConfirmationTitle"),
                PluginBundle.message(
                    "adbCommandConfirmationMessage",
                    command.name,
                    device.titleText,
                    fullCommand
                )
            )
                .icon(Messages.getQuestionIcon())
                .doNotAsk(object : com.intellij.openapi.ui.DoNotAskOption {
                    override fun isToBeShown() = true
                    override fun setToBeShown(toBeShown: Boolean, exitCode: Int) {
                        if (!toBeShown && exitCode == Messages.YES) {
                            disableConfirmationForCommand(command)
                        }
                    }
                    override fun canBeHidden() = true
                    override fun shouldSaveOptionsOnCancel() = false
                    override fun getDoNotShowMessage() =
                        PluginBundle.message("adbCommandConfirmationCheckbox")
                })
                .ask(project)

            if (!result) return
        }

        launch {
            withContext(IO) {
                adbService.executeCommand(command, device.id, packageName)
            }
        }
    }

    private fun disableConfirmationForCommand(command: AdbCommandConfig) {
        val updatedCommands = adbCommandsService.commands.map {
            if (it.id == command.id) {
                it.copy(requiresConfirmation = false)
            } else {
                it
            }
        }
        adbCommandsService.commands = updatedCommands
    }

    private fun onDevicesUpdated(model: List<Device>) {
        devices = model.map {
            it.toViewModel(customName = deviceNamesService.findName(it.serialNumber))
        }
        pinnedDevices = pinDeviceService.pinnedDevices.toViewModel()

        updateDeviceLists()
    }

    private fun updateDeviceLists() {
        val isScrcpyEnabled = propertiesService.isScrcpyEnabled.value
        val packageName = packageService.getPackageName()

        devices.forEach {
            it.isInProgress = connectingDevices.contains(it)
            it.isShareScreenButtonVisible = isScrcpyEnabled
            it.isAdbCommandsButtonVisible = true
            it.packageName = packageName
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
        logVisibilityJob = launch {
            logService.isLogVisible.collect { isVisible ->
                updateLogVisibility(isVisible)
            }
        }
    }

    private fun unsubscribeFromLogEvents() {
        logVisibilityJob?.cancel()
        logVisibilityJob = null
    }

    private fun subscribeToScrcpyEnabledState() {
        scrcpyEnabledJob = launch {
            propertiesService.isScrcpyEnabled.collect {
                updateDeviceLists()
            }
        }
    }

    private fun unsubscribeFromScrcpyEnabledState() {
        scrcpyEnabledJob?.cancel()
        scrcpyEnabledJob = null
    }

    private fun updateLogVisibility(isLogVisible: Boolean) {
        if (isLogVisible) {
            view?.openLog()
            logEntriesJob = launch {
                logService.commandHistory.entries.collect { entries ->
                    view?.setLogEntries(entries)
                }
            }
        } else {
            view?.closeLog()
            logEntriesJob?.cancel()
            logEntriesJob = null
        }
    }

    private fun subscribeToAdbLocationChanges() {
        adbLocationJob = launch {
            propertiesService.isAdbLocationValid.collect { isValid ->
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
    }

    private fun unsubscribeFromAdbLocationChanges() {
        adbLocationJob?.cancel()
        adbLocationJob = null
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
