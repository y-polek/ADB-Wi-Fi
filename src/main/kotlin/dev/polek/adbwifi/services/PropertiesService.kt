package dev.polek.adbwifi.services

interface PropertiesService {
    var isLogVisible: Boolean

    var isPreviouslyConnectedDevicesExpanded: Boolean

    var useAdbFromPath: Boolean
    var adbLocation: String
    val defaultAdbLocation: String

    var scrcpyEnabled: Boolean
    val defaultScrcpyEnabled: Boolean
    var useScrcpyFromPath: Boolean
    var scrcpyLocation: String
    val defaultScrcpyLocation: String
    var scrcpyCmdFlags: String

    var adbLocationListener: ((isValid: Boolean) -> Unit)?
    var scrcpyEnabledListener: ((isEnabled: Boolean) -> Unit)?
}
