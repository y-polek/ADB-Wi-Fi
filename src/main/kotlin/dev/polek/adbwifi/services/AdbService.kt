package dev.polek.adbwifi.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import dev.polek.adbwifi.adb.ADB_DISPATCHER
import dev.polek.adbwifi.adb.Adb
import dev.polek.adbwifi.commandexecutor.RuntimeCommandExecutor
import dev.polek.adbwifi.model.AdbCommandConfig
import dev.polek.adbwifi.model.Device
import dev.polek.adbwifi.utils.appCoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Service
class AdbService {

    private val adb = Adb(RuntimeCommandExecutor(), service())
    private val logService by lazy { service<LogService>() }
    private val pinDeviceService by lazy { service<PinDeviceService>() }
    private val properties by lazy { service<PropertiesService>() }

    val devices: StateFlow<List<Device>> = flow {
        while (true) {
            emit(fetchDevices())
            delay(POLLING_INTERVAL_MILLIS)
        }
    }.stateIn(
        scope = appCoroutineScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = emptyList()
    )

    private suspend fun fetchDevices(): List<Device> {
        val devices = withContext(ADB_DISPATCHER) {
            adb.devices()
        }
        pinDeviceService.addPreviouslyConnectedDevices(devices)
        return devices
    }

    suspend fun connect(device: Device) {
        adb.connect(device).collect { logEntry ->
            logService.commandHistory.add(logEntry)
        }
    }

    suspend fun connect(ip: String, port: Int = properties.adbPort): String {
        val logEntries = adb.connect(ip, port).toList()
        logEntries.forEach { logService.commandHistory.add(it) }
        return logEntries.lastOrNull()?.text.orEmpty()
    }

    suspend fun disconnect(device: Device) {
        adb.disconnect(device).collect { logEntry ->
            logService.commandHistory.add(logEntry)
        }
    }

    fun restartAdb() {
        appCoroutineScope.launch(Dispatchers.Default) {
            adb.disconnectAllDevices().collect { logEntry ->
                logService.commandHistory.add(logEntry)
            }
            adb.killServer().collect { logEntry ->
                logService.commandHistory.add(logEntry)
            }
        }
    }

    suspend fun executeCommand(
        config: AdbCommandConfig,
        deviceId: String,
        packageName: String,
        parameterValues: Map<String, String> = emptyMap()
    ) {
        val commands = config.command.split("\n").filter { it.isNotBlank() }
        commands.forEachIndexed { index, cmd ->
            var command = cmd.trim().replace("{package}", packageName)
            parameterValues.forEach { (placeholder, value) ->
                command = command.replace(placeholder, value)
            }
            adb.executeCommand(deviceId, command).collect { logEntry ->
                logService.commandHistory.add(logEntry)
            }
            if (index < commands.lastIndex) {
                delay(500)
            }
        }
    }

    fun listPackages(deviceId: String): List<String> {
        return adb.listPackages(deviceId)
    }

    private companion object {
        const val POLLING_INTERVAL_MILLIS = 3000L
    }
}
