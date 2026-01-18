package dev.polek.adbwifi.services

import kotlinx.coroutines.flow.StateFlow

interface PropertiesService {
    var isLogVisible: Boolean
    var isLogWrapContent: Boolean

    var isPreviouslyConnectedDevicesExpanded: Boolean
    var confirmDeviceRemoval: Boolean

    var useAdbFromPath: Boolean
    val defaultAdbLocation: String
    var adbLocation: String
    var adbPort: Int

    val defaultScrcpyEnabled: Boolean
    val isScrcpyEnabled: StateFlow<Boolean>
    fun setScrcpyEnabled(enabled: Boolean)
    var useScrcpyFromPath: Boolean
    var scrcpyLocation: String
    val defaultScrcpyLocation: String
    var scrcpyCmdFlags: String

    val isAdbLocationValid: StateFlow<Boolean>
}
