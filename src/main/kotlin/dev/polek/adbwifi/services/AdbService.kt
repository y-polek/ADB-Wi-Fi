package dev.polek.adbwifi.services

import com.intellij.openapi.Disposable
import dev.polek.adbwifi.LOG
import dev.polek.adbwifi.adb.ADB_DISPATCHER
import dev.polek.adbwifi.adb.Adb
import dev.polek.adbwifi.commandexecutor.RuntimeCommandExecutor
import dev.polek.adbwifi.model.CommandHistory
import dev.polek.adbwifi.model.Device
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class AdbService : Disposable {

    var deviceListListener: ((List<Device>) -> Unit)? = null
        set(value) {
            field = value
            if (value != null) {
                startPollingDevices()
            } else {
                stopPollingDevices()
            }
        }

    val commandHistory = CommandHistory()

    private val adb = Adb(RuntimeCommandExecutor())
    private var devicePollingJob: Job? = null

    fun refreshDeviceList() {
        startPollingDevices()
    }

    fun connect(device: Device) = GlobalScope.launch(ADB_DISPATCHER) {
        adb.connect(device).collect { logEntry ->
            commandHistory.add(logEntry)
        }
        withContext(Dispatchers.Main) {
            delay(1000)
            refreshDeviceList()
        }
    }

    fun disconnect(device: Device) = GlobalScope.launch(ADB_DISPATCHER) {
        adb.disconnect(device).collect { logEntry ->
            commandHistory.add(logEntry)
        }
        withContext(Dispatchers.Main) {
            delay(1000)
            refreshDeviceList()
        }
    }

    override fun dispose() {
        stopPollingDevices()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun startPollingDevices() {
        devicePollingJob?.cancel()
        devicePollingJob = GlobalScope.launch(Dispatchers.Main) {
            devicesFlow()
                    .flowOn(ADB_DISPATCHER)
                    .collect { devices ->
                        LOG.warn("devices: $devices")
                        deviceListListener?.invoke(devices)
                    }
        }
    }

    private fun stopPollingDevices() {
        devicePollingJob?.cancel()
        devicePollingJob = null
    }

    private fun devicesFlow(): Flow<List<Device>> = flow {
        while (true) {
            emit(adb.devices())
            delay(POLLING_INTERVAL_MILLIS)
        }
    }

    private companion object {
        const val POLLING_INTERVAL_MILLIS = 3000L
    }
}
