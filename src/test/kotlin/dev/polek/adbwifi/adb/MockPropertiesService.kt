package dev.polek.adbwifi.adb

import dev.polek.adbwifi.services.PropertiesService

class MockPropertiesService(
    override var isLogVisible: Boolean = false,
    override var useAdbFromPath: Boolean = false,
    override var adbLocation: String = "/bin",
    override var defaultAdbLocation: String = "/bin",
    override var adbLocationListener: ((isValid: Boolean) -> Unit)? = null,
    override var useScrcpyFromPath: Boolean = true,
    override var scrcpyLocation: String = "/bin",
    override val defaultScrcpyLocation: String = ""
) : PropertiesService
