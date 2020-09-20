package dev.polek.adbwifi.services

interface PropertiesService {
    var isLogVisible: Boolean

    var isPreviouslyConnectedDevicesExpanded: Boolean

    var useAdbFromPath: Boolean
    var adbLocation: String
    val defaultAdbLocation: String

    var useScrcpyFromPath: Boolean
    var scrcpyLocation: String
    val defaultScrcpyLocation: String

    var adbLocationListener: ((isValid: Boolean) -> Unit)?
}
