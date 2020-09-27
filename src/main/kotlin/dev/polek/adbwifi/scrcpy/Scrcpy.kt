package dev.polek.adbwifi.scrcpy

import dev.polek.adbwifi.LOG
import dev.polek.adbwifi.commandexecutor.CommandExecutor
import dev.polek.adbwifi.model.Device
import dev.polek.adbwifi.services.PropertiesService
import dev.polek.adbwifi.utils.adbExec
import dev.polek.adbwifi.utils.findAdbExecInSystemPath
import dev.polek.adbwifi.utils.findScrcpyExecInSystemPath
import dev.polek.adbwifi.utils.scrcpyExec
import java.io.IOException

class Scrcpy(
    private val commandExecutor: CommandExecutor,
    private val properties: PropertiesService
) {
    private val scrcpyExe: String?
        get() {
            return if (properties.useScrcpyFromPath) {
                findScrcpyExecInSystemPath()
            } else {
                scrcpyExec(properties.scrcpyLocation)
            }
        }

    private val adbExe: String
        get() {
            return if (properties.useAdbFromPath) {
                findAdbExecInSystemPath()
                    ?: throw IllegalStateException("Cannot find 'adb' executable in system PATH")
            } else {
                adbExec(properties.adbLocation)
            }
        }

    val isValid: Boolean
        get() {
            val scrcpyExe = scrcpyExe ?: return false
            val command = "$scrcpyExe --version"
            return try {
                commandExecutor.testExec(command, "ADB=$adbExe")
            } catch (e: IOException) {
                LOG.warn("Failed to execute command '$command': ${e.message}")
                false
            }
        }

    fun share(device: Device) {
        val scrcpyExe = scrcpyExe ?: throw IllegalStateException("'scrcpy' executable not found")
        val command = "$scrcpyExe -s ${device.id}"
        try {
            commandExecutor.exec(command, "ADB=$adbExe")
        } catch (e: IOException) {
            LOG.warn("Failed to execute command '$command': ${e.message}")
        }
    }
}
