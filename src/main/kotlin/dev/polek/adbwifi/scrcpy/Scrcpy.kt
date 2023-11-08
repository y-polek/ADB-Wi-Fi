package dev.polek.adbwifi.scrcpy

import dev.polek.adbwifi.LOG
import dev.polek.adbwifi.commandexecutor.CmdResult
import dev.polek.adbwifi.commandexecutor.CommandExecutor
import dev.polek.adbwifi.model.Device
import dev.polek.adbwifi.services.PropertiesService
import dev.polek.adbwifi.utils.*
import kotlinx.coroutines.delay
import java.io.IOException
import java.io.OutputStream

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
                checkNotNull(findAdbExecInSystemPath()) {
                    "Cannot find 'adb' executable in system PATH"
                }
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

    suspend fun share(device: Device): CmdResult {
        val scrcpyExe = checkNotNull(scrcpyExe) { "'scrcpy' executable not found" }
        val processBuilder = ProcessBuilder(listOf(scrcpyExe, "-s", device.id) + cmdFlags())
            .redirectErrorStream(true)
        processBuilder.environment()["ADB"] = adbExe

        return try {
            val process = processBuilder.start()
            while (process.isAlive) {
                delay(1000)

                // Write new-line symbol to unblock process in case of "Press any key to continue..." prompt
                process.outputStream.writeNewLine()
            }
            val exitCode = process.exitValue()
            val output = process.output()
            LOG.info("scrcpy finished ($exitCode): $output")
            CmdResult(exitCode, output)
        } catch (e: IOException) {
            LOG.warn("Failed to execute command '${processBuilder.command().joinToString(" ")}': ${e.message}", e)
            CmdResult(-1, e.message.orEmpty())
        }
    }

    private fun cmdFlags(): List<String> {
        return properties.scrcpyCmdFlags.split(WHITESPACE_REGEX)
            .filter(String::isNotBlank)
    }

    private companion object {
        private val WHITESPACE_REGEX = "\\s+".toRegex()

        private fun OutputStream.writeNewLine() = try {
            this.write(System.lineSeparator().toByteArray())
            this.flush()
        } catch (e: IOException) {
            LOG.warn(e)
        }
    }
}
