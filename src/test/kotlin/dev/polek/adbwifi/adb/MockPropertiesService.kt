package dev.polek.adbwifi.adb

import dev.polek.adbwifi.services.PropertiesService

class MockPropertiesService(
    override var isLogVisible: Boolean = false,
    override var adbLocation: String = "/bin",
    override var adbLocationListener: ((location: String, isValid: Boolean) -> Unit)? = null
) : PropertiesService
