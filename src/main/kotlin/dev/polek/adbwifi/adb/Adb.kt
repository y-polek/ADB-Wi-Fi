package dev.polek.adbwifi.adb

import dev.polek.adbwifi.commandexecutor.CommandExecutor
import dev.polek.adbwifi.model.Device
import dev.polek.adbwifi.model.LogEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow

class Adb(private val commandExecutor: CommandExecutor) {

    fun devices(): List<Device> {
        val devices = "adb devices".exec()
            .drop(1)
            .mapNotNull { line ->
                DEVICE_ID_REGEX.matchEntire(line)?.groupValues?.get(1)?.trim()
            }
            .map { deviceId ->
                val androidId = androidId(deviceId)
                val model = model(deviceId)
                val manufacturer = manufacturer(deviceId)
                val address = address(deviceId)
                Device(
                    id = deviceId,
                    androidId = androidId,
                    name = "$manufacturer $model".trim(),
                    address = address,
                    androidVersion = androidVersion(deviceId),
                    apiLevel = apiLevel(deviceId),
                    connectionType = connectionType(deviceId)
                )
            }
            .toList()

        devices.forEach { device ->
            device.isConnected = when {
                device.isWifiDevice -> true
                device.androidId.isBlank() -> false
                else -> {
                    val wifiDevice = devices.firstOrNull {
                        it.isWifiDevice && it.androidId == device.androidId
                    }
                    wifiDevice != null
                }
            }
        }

        return devices.sortedWith(DEVICE_COMPARATOR)
    }

    fun connect(device: Device): Flow<LogEntry> = flow {
        "adb -s ${device.id} tcpip 5555".execAndLog(this)
        "adb connect ${device.address}:5555".execAndLog(this)
    }

    fun disconnect(device: Device): Flow<LogEntry> = flow {
        "adb disconnect ${device.address}:5555".execAndLog(this)
    }

    private fun androidId(deviceId: String): String {
        return "adb -s $deviceId shell settings get secure android_id".exec().firstLine()
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

    private fun address(deviceId: String): String? {
        val firstLine = "adb -s $deviceId shell ip route".exec().firstLine()
        return DEVICE_ADDRESS_REGEX.matchEntire(firstLine)?.groupValues?.get(1)
    }

    private fun connectionType(deviceId: String): Device.ConnectionType {
        return when {
            IS_IP_ADDRESS_REGEX.matches(deviceId) -> Device.ConnectionType.WIFI
            else -> Device.ConnectionType.USB
        }
    }

    private fun String.exec(): Sequence<String> = commandExecutor.exec(this)

    private fun String.execSilently(): String {
        return this.exec().joinToString(separator = "\n")
    }

    private suspend fun String.execAndLog(logCollector: FlowCollector<LogEntry>) {
        logCollector.emit(LogEntry.Command(this))
        val output = this.execSilently()
        logCollector.emit(LogEntry.Output(output))
    }

    companion object {
        private val DEVICE_ID_REGEX = "(.*?)\\s+device".toRegex()
        private val DEVICE_ADDRESS_REGEX = ".*\\b(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})\\b.*".toRegex()
        private val IS_IP_ADDRESS_REGEX = "(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})(:\\d{1,5})?".toRegex()

        private val DEVICE_COMPARATOR = compareBy<Device>({ it.name }, { it.isWifiDevice })

        private fun Sequence<String>.firstLine(): String = this.firstOrNull().orEmpty()
    }
}
