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
                    val model = model(deviceId)
                    val manufacturer = manufacturer(deviceId)
                    val address = address(deviceId)
                    Device(
                            id = deviceId,
                            name = "$manufacturer $model".trim(),
                            address = address)
                }
                .toList()
    }

    private fun model(deviceId: String): String {
        return "adb -s $deviceId shell getprop ro.product.model".exec().firstLine().trim()
    }

    private fun manufacturer(deviceId: String): String {
        return "adb -s $deviceId shell getprop ro.product.manufacturer".exec().firstLine().trim()
    }

    private fun address(deviceId: String): String {
        val address = DEVICE_ADDRESS_REGEX.matchEntire("adb -s $deviceId shell ip route".exec().firstLine())?.groupValues?.get(1)
        return address.orEmpty()
    }

    companion object {
        private val DEVICE_ID_REGEX = "(.*?)\\s+device".toRegex()
        private val DEVICE_ADDRESS_REGEX = ".*\\b(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})\\b.*".toRegex()

        private fun String.exec(): Sequence<String> {
            val process = Runtime.getRuntime().exec(this)
            return BufferedReader(InputStreamReader(process.inputStream)).lineSequence()
        }

        private fun Sequence<String>.firstLine(): String = this.firstOrNull().orEmpty()
    }
}
