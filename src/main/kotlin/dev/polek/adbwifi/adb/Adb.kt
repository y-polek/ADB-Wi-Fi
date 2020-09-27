package dev.polek.adbwifi.adb

import dev.polek.adbwifi.LOG
import dev.polek.adbwifi.commandexecutor.CommandExecutor
import dev.polek.adbwifi.model.Device
import dev.polek.adbwifi.model.Device.ConnectionType.USB
import dev.polek.adbwifi.model.LogEntry
import dev.polek.adbwifi.services.PropertiesService
import dev.polek.adbwifi.utils.adbExec
import dev.polek.adbwifi.utils.findAdbExecInSystemPath
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import java.io.IOException

@OptIn(FlowPreview::class)
class Adb(
    private val commandExecutor: CommandExecutor,
    private val properties: PropertiesService
) {
    fun devices(): List<Device> {
        val devices = "devices".exec()
            .drop(1)
            .mapNotNull { line ->
                DEVICE_ID_REGEX.matchEntire(line)?.groupValues?.get(1)?.trim()
            }
            .map { deviceId ->
                val model = model(deviceId).trim()
                val manufacturer = manufacturer(deviceId).trim()
                Device(
                    id = deviceId,
                    serialNumber = serialNumber(deviceId),
                    name = "$manufacturer $model",
                    address = address(deviceId),
                    androidVersion = androidVersion(deviceId),
                    apiLevel = apiLevel(deviceId),
                    connectionType = connectionType(deviceId)
                )
            }
            .toList()

        devices.forEach { device ->
            device.isConnected = when {
                device.isWifiDevice -> true
                device.serialNumber.isBlank() -> false
                else -> {
                    val wifiDevice = devices.firstOrNull {
                        it.isWifiDevice && it.serialNumber == device.serialNumber
                    }
                    wifiDevice != null
                }
            }
        }

        return devices.sortedWith(DEVICE_COMPARATOR)
    }

    fun connect(device: Device): Flow<LogEntry> = flow<LogEntry> {
        if (device.connectionType == USB) {
            "-s ${device.id} tcpip 5555".execAndLogAsync(this)
            delay(1000)
        }
        try {
            "connect ${device.address}:5555".execAndLogAsync(this@flow)
        } catch (e: TimeoutCancellationException) {
            emit(LogEntry.Output("Timed out"))
        }
    }

    fun disconnect(device: Device): Flow<LogEntry> = flow<LogEntry> {
        try {
            "disconnect ${device.address}:5555".execAndLogAsync(this@flow)
        } catch (e: TimeoutCancellationException) {
            emit(LogEntry.Output("Timed out"))
        }
    }

    fun killServer(): Flow<LogEntry> = flow {
        "kill-server".execAndLog(this)
    }

    private fun serialNumber(deviceId: String): String {
        return "-s $deviceId shell getprop ro.serialno".exec().firstLine()
    }

    private fun model(deviceId: String): String {
        return "-s $deviceId shell getprop ro.product.model".exec().firstLine().trim()
    }

    private fun manufacturer(deviceId: String): String {
        return "-s $deviceId shell getprop ro.product.manufacturer".exec().firstLine().trim()
    }

    private fun androidVersion(deviceId: String): String {
        return "-s $deviceId shell getprop ro.build.version.release".exec().firstLine().trim()
    }

    private fun apiLevel(deviceId: String): String {
        return "-s $deviceId shell getprop ro.build.version.sdk".exec().firstLine().trim()
    }

    private fun address(deviceId: String): String? {
        val firstLine = "-s $deviceId shell ip route".exec().firstLine()
        return DEVICE_ADDRESS_REGEX.matchEntire(firstLine)?.groupValues?.get(1)
    }

    private fun connectionType(deviceId: String): Device.ConnectionType {
        return when {
            IS_IP_ADDRESS_REGEX.matches(deviceId) -> Device.ConnectionType.WIFI
            else -> USB
        }
    }

    private fun adbCommand(args: String): String {
        val adb = if (properties.useAdbFromPath) {
            findAdbExecInSystemPath() ?: throw IllegalStateException("Cannot find 'adb' executable in system PATH")
        } else {
            adbExec(properties.adbLocation)
        }
        return "$adb $args"
    }

    private fun String.exec(): Sequence<String> {
        val command = adbCommand(this)
        return try {
            commandExecutor.exec(command)
        } catch (e: IOException) {
            LOG.warn("Failed to execute command '$command': ${e.message}")
            emptySequence()
        }
    }

    private suspend fun String.execAndLog(logCollector: FlowCollector<LogEntry>): String {
        logCollector.emit(LogEntry.Command(this))
        val output = this.exec().joinToString("\n")
        logCollector.emit(LogEntry.Output(output))
        return output
    }

    private suspend fun String.execAndLogAsync(logCollector: FlowCollector<LogEntry>) {
        val command = adbCommand(this)
        logCollector.emit(LogEntry.Command(this))
        val output = try {
            commandExecutor.execAsync(command)
        } catch (e: IOException) {
            LOG.warn("Failed to execute command '$command': ${e.message}")
            e.message ?: "Failed to execute command"
        }
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
