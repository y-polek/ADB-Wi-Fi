package dev.polek.adbwifi.adb

import dev.polek.adbwifi.services.PropertiesService

class MockPropertiesService(
    override val isLogVisible: Boolean = false,
    override val adbLocation: String = "/bin"
) : PropertiesService
