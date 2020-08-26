package dev.polek.adbwifi.adb

import dev.polek.adbwifi.services.PropertiesService

class MockPropertiesService(
    override var isLogVisible: Boolean = false,
    override var adbLocation: String = "/bin"
) : PropertiesService
