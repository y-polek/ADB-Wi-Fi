package dev.polek.adbwifi.adb

import dev.polek.adbwifi.LOG
import dev.polek.adbwifi.commandexecutor.CommandExecutor
import dev.polek.adbwifi.model.Address
import dev.polek.adbwifi.model.Device
import dev.polek.adbwifi.model.Device.ConnectionType.*
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
import org.jetbrains.annotations.TestOnly
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
            .flatMap { deviceId ->
                addresses(deviceId).asSequence().map { address -> deviceId to address }
            }
            .mapNotNull { (deviceId, address) ->
                val addressFromId = IP_ADDRESS_REGEX.matchEntire(deviceId)?.groupValues?.getOrNull(1)
                val connectionType = when {
                    addressFromId != null -> WIFI
                    else -> USB
                }
                if (connectionType == WIFI && address.ip != addressFromId) return@mapNotNull null

                val model = model(deviceId).trim()
                val manufacturer = manufacturer(deviceId).trim()
                Device(
                    id = deviceId,
                    serialNumber = serialNumber(deviceId),
                    name = "$manufacturer $model",
                    address = address,
                    androidVersion = androidVersion(deviceId),
                    apiLevel = apiLevel(deviceId),
                    connectionType = connectionType
                )
            }
            .toList()

        devices.forEach { device ->
            device.isConnected = when {
                device.isWifiDevice -> true
                device.serialNumber.isBlank() -> false
                else -> {
                    val wifiDevice = devices.firstOrNull {
                        it.isWifiDevice && it.serialNumber == device.serialNumber && it.address == device.address
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
            "connect ${device.address?.ip}:5555".execAndLogAsync(this@flow)
        } catch (e: TimeoutCancellationException) {
            emit(LogEntry.Output("Timed out"))
        }
    }

    fun disconnect(device: Device): Flow<LogEntry> = flow<LogEntry> {
        try {
            "disconnect ${device.address?.ip}:5555".execAndLogAsync(this@flow)
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

    @TestOnly
    fun addresses(deviceId: String): List<Address> {
        return "-s $deviceId shell ip route".exec()
            .mapNotNull {
                val groups = ADDRESS_LINE_REGEX.matchEntire(it)?.groupValues ?: return@mapNotNull null
                if (groups.size < 3) return@mapNotNull null
                return@mapNotNull Address(interfaceName = groups[1], ip = groups[2])
            }
            .sortedWith(ADDRESS_COMPARATOR)
            .toList()
    }

    private fun adbCommand(args: String): String {
        val adb = if (properties.useAdbFromPath) {
            findAdbExecInSystemPath() ?: run {
                LOG.warn("Cannot find 'adb' executable in system PATH")
                "adb"
            }
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
        private val ADDRESS_LINE_REGEX =
            ".*dev\\s*(\\S+)\\s*.*\\b(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})\\b.*".toRegex()
        private val IP_ADDRESS_REGEX = "(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})(:\\d{1,5})".toRegex()
        private val ADDRESS_COMPARATOR = compareBy<Address> { it.isWlan }.reversed().thenBy { it.interfaceName }

        private val DEVICE_COMPARATOR = compareBy<Device>({ it.name }, { it.isWifiDevice })

        private fun Sequence<String>.firstLine(): String = this.firstOrNull().orEmpty()
    }
}
