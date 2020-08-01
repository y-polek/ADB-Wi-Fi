package dev.polek.adbwifi.services

import com.intellij.openapi.Disposable
import dev.polek.adbwifi.adb.Adb
import dev.polek.adbwifi.model.Device

class AdbService : Disposable {

    private val adb = Adb()

    fun devices(): List<Device> = adb.devices()

    fun connect(device: Device) {

    }

    fun disconnect(device: Device) {

    }

    override fun dispose() {

    }
}
