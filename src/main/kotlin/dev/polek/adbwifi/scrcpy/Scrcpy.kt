package dev.polek.adbwifi.scrcpy

import dev.polek.adbwifi.LOG
import dev.polek.adbwifi.commandexecutor.CommandExecutor
import dev.polek.adbwifi.model.Device
import dev.polek.adbwifi.services.PropertiesService
import java.io.IOException

class Scrcpy(
    private val commandExecutor: CommandExecutor,
    private val properties: PropertiesService
) {
    private val scrcpyExe: String
        get() = if (properties.useScrcpyFromPath) "scrcpy" else "${properties.scrcpyLocation}/scrcpy"

    fun share(device: Device) {
        val command = "$scrcpyExe -s ${device.id}"
        try {
            commandExecutor.exec(command)
        } catch (e: IOException) {
            LOG.warn("Failed to execute command '$command': ${e.message}")
        }
    }
}
