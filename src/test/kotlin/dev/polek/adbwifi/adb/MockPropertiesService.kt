package dev.polek.adbwifi.adb

import dev.polek.adbwifi.services.PropertiesService
import dev.polek.adbwifi.utils.ADB_DEFAULT_PORT
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@SuppressWarnings("LongParameterList")
class MockPropertiesService(
    override var isLogVisible: Boolean = false,
    override var isPreviouslyConnectedDevicesExpanded: Boolean = true,
    override var confirmDeviceRemoval: Boolean = false,
    override var useAdbFromPath: Boolean = false,
    override var adbLocation: String = "/bin",
    override var defaultAdbLocation: String = "/bin",
    override var adbPort: Int = ADB_DEFAULT_PORT,
    isScrcpyEnabled: Boolean = true,
    override val defaultScrcpyEnabled: Boolean = true,
    override var useScrcpyFromPath: Boolean = true,
    override var scrcpyLocation: String = "/bin",
    override val defaultScrcpyLocation: String = "",
    override var scrcpyCmdFlags: String = "",
    isAdbLocationValid: Boolean = true
) : PropertiesService {

    private val _isScrcpyEnabled = MutableStateFlow(isScrcpyEnabled)
    override val isScrcpyEnabled: StateFlow<Boolean> = _isScrcpyEnabled

    override fun setScrcpyEnabled(enabled: Boolean) {
        _isScrcpyEnabled.value = enabled
    }

    private val _isAdbLocationValid = MutableStateFlow(isAdbLocationValid)
    override val isAdbLocationValid: StateFlow<Boolean> = _isAdbLocationValid
}
