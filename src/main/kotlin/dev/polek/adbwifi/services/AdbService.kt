package dev.polek.adbwifi.services

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.logger
import dev.polek.adbwifi.adb.Adb
import dev.polek.adbwifi.model.CommandHistory
import dev.polek.adbwifi.model.Device

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

    private val adb = Adb()
    private var devicesPollingThread: DevicesPollingThread? = null

    fun refreshDeviceList() {
        devicesPollingThread?.poll()
    }

    fun connect(device: Device) {
        commandHistory += adb.connect(device)
        refreshDeviceList()
    }

    fun disconnect(device: Device) {
        commandHistory += adb.disconnect(device)
        refreshDeviceList()
    }

    override fun dispose() {
        stopPollingDevices()
    }

    private fun startPollingDevices() {
        devicesPollingThread = object : DevicesPollingThread(adb) {
            override fun onResult(devices: List<Device>) {
                ApplicationManager.getApplication().invokeLater {
                    log.info("${devices.size} devices")
                    deviceListListener?.invoke(devices)
                }
            }
        }
        devicesPollingThread?.start()
    }

    private fun stopPollingDevices() {
        devicesPollingThread?.cancel()
        devicesPollingThread = null
    }

    private abstract class DevicesPollingThread(val adb: Adb) : Thread() {

        private var canceled = false

        fun cancel() {
            canceled = true
        }

        fun poll() {
            interrupt()
        }

        abstract fun onResult(devices: List<Device>)

        override fun run() {
            while (!canceled) {
                log.info("Running adb devices")
                val devices = adb.devices()
                if (canceled) return

                onResult(devices)

                try {
                    log.info("Sleeping for 2 seconds")
                    sleep(5000)
                } catch (e: InterruptedException) {
                    // Pass
                }
            }
        }
    }

    companion object {
        private val log = logger("AdbService")
    }
}
