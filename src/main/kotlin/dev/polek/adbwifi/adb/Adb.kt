package dev.polek.adbwifi.adb

import dev.polek.adbwifi.model.Device
import java.io.BufferedReader
import java.io.InputStreamReader

class Adb {

    fun devices(): List<Device> {
        return "adb devices".exec()
                .drop(1)
                .mapNotNull { line ->
                    DEVICE_ID_REGEX.matchEntire(line)?.groupValues?.get(1)
                }
                .map { deviceId ->
                    val model = "adb -s $deviceId shell getprop ro.product.model".exec().firstOrNull().orEmpty()
                    val manufacturer = "adb -s $deviceId shell getprop ro.product.manufacturer".exec().firstOrNull().orEmpty()
                    val address = "adb -s $deviceId shell ip route | awk '{print \$9}'".exec().firstOrNull().orEmpty()
                    Device(
                            id = deviceId,
                            name = "$manufacturer $model".trim(),
                            address = address)
                }
                .toList()
    }

    companion object {
        private val DEVICE_ID_REGEX = "(.*?)\\s+device".toRegex()

        private fun String.exec(): Sequence<String> {
            val process = Runtime.getRuntime().exec(this)
            return BufferedReader(InputStreamReader(process.inputStream)).lineSequence()
        }
    }
}
