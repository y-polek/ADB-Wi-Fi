package dev.polek.adbwifi.adb

import com.intellij.openapi.diagnostic.logger
import dev.polek.adbwifi.model.Device
import dev.polek.adbwifi.model.LogEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import java.io.BufferedReader
import java.io.InputStreamReader

class Adb {

    fun devices(): List<Device> {
        return "adb devices".exec()
            .drop(1)
            .mapNotNull { line ->
                DEVICE_ID_REGEX.matchEntire(line)?.groupValues?.get(1)?.trim()
            }
            .map { deviceId ->
                val model = model(deviceId)
                val manufacturer = manufacturer(deviceId)
                val address = address(deviceId)
                Device(
                    id = deviceId,
                    name = "$manufacturer $model".trim(),
                    address = address,
                    androidVersion = androidVersion(deviceId),
                    apiLevel = apiLevel(deviceId),
                    isConnected = isConnected(deviceId)
                )
            }
            .toList()
    }

    fun connect(device: Device): Flow<LogEntry> = flow {
        if (device.isConnected) {
            log.warn("Device $device is already connected")
            return@flow
        }

        "adb -s ${device.id} tcpip 5555".execAndLog(this)
        "adb connect ${device.address}:5555".execAndLog(this)
    }

    fun disconnect(device: Device): Flow<LogEntry> = flow {
        if (!device.isConnected) {
            log.warn("Device $device is already disconnected")
            return@flow
        }

        "adb disconnect ${device.address}:5555".execAndLog(this)
    }

    private fun model(deviceId: String): String {
        return "adb -s $deviceId shell getprop ro.product.model".exec().firstLine().trim()
    }

    private fun manufacturer(deviceId: String): String {
        return "adb -s $deviceId shell getprop ro.product.manufacturer".exec().firstLine().trim()
    }

    private fun androidVersion(deviceId: String): String {
        return "adb -s $deviceId shell getprop ro.build.version.release".exec().firstLine().trim()
    }

    private fun apiLevel(deviceId: String): String {
        return "adb -s $deviceId shell getprop ro.build.version.sdk".exec().firstLine().trim()
    }

    private fun address(deviceId: String): String {
        val firstLine = "adb -s $deviceId shell ip route".exec().firstLine()
        val address = DEVICE_ADDRESS_REGEX.matchEntire(firstLine)?.groupValues?.get(1)
        return address.orEmpty()
    }

    private fun isConnected(deviceId: String): Boolean {
        return IS_DEVICE_CONNECTED_REGEX.matches(deviceId)
    }

    companion object {
        private val log = logger("Adb")

        private val DEVICE_ID_REGEX = "(.*?)\\s+device".toRegex()
        private val DEVICE_ADDRESS_REGEX = ".*\\b(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})\\b.*".toRegex()
        private val IS_DEVICE_CONNECTED_REGEX = "(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})(:\\d{1,5})?".toRegex()

        private fun String.exec(): Sequence<String> {
            val process = Runtime.getRuntime().exec(this)
            return BufferedReader(InputStreamReader(process.inputStream)).lineSequence()
        }

        private fun String.execSilently(): String {
            return this.exec().joinToString(separator = "\n")
        }

        private suspend fun String.execAndLog(logCollector: FlowCollector<LogEntry>) {
            logCollector.emitCommand(this)
            val output = this.execSilently()
            logCollector.emitOutput(output)
        }

        private suspend fun FlowCollector<LogEntry>.emitCommand(command: String) {
            emit(LogEntry.Command(command))
        }

        private suspend fun FlowCollector<LogEntry>.emitOutput(output: String) {
            emit(LogEntry.Output(output))
        }

        private fun Sequence<String>.firstLine(): String = this.firstOrNull().orEmpty()
    }
}
