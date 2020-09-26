package dev.polek.adbwifi.commandexecutor

import kotlinx.coroutines.delay
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

class RuntimeCommandExecutor : CommandExecutor {

    private val runtime = Runtime.getRuntime()

    override fun exec(command: String, vararg envp: String): Sequence<String> {
        val process = runtime.exec(command, envp)
        return BufferedReader(InputStreamReader(process.inputStream)).lineSequence()
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun execAsync(command: String, vararg envp: String): String {
        val process = runtime.exec(command, envp)
        while (process.isAlive) {
            delay(500L)
        }
        return BufferedReader(InputStreamReader(process.inputStream))
            .lineSequence()
            .joinToString(separator = "\n")
    }

    override fun textExec(command: String, vararg envp: String): Boolean {
        val process = try {
            runtime.exec(command, envp)
        } catch (e: IOException) {
            return false
        }
        process.waitFor()
        return process.exitValue() == 0
    }
}
