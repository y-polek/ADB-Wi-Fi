package dev.polek.adbwifi.commandexecutor

import dev.polek.adbwifi.LOG
import kotlinx.coroutines.delay
import java.io.IOException

class RuntimeCommandExecutor : CommandExecutor {

    private val runtime = Runtime.getRuntime()

    override fun exec(command: String, vararg envp: String): Sequence<String> {
        log { "exec> $command ${envp.joinToString()}" }

        val process = runtime.exec(command, envp)
        return process.inputStream.bufferedReader().lineSequence()
    }

    override fun execSilently(command: String, vararg envp: String) {
        log { "execSilently> $command ${envp.joinToString()}" }

        runtime.exec(command, envp)
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun execAsync(command: String, vararg envp: String): String {
        log { "execAsync> $command ${envp.joinToString()}" }

        val process = runtime.exec(command, envp)
        while (process.isAlive) {
            delay(500L)
        }
        return process.output()
    }

    override fun testExec(command: String, vararg envp: String): Boolean {
        log { "testExec> $command ${envp.joinToString()}" }

        val process = try {
            runtime.exec(command, envp)
        } catch (e: IOException) {
            return false
        }
        process.waitFor()
        return process.exitValue() == 0
    }

    private companion object {

        private const val DEBUG_ENABLED = false

        private inline fun log(lazyMessage: () -> String) {
            if (DEBUG_ENABLED) {
                LOG.warn(lazyMessage())
            }
        }

        private fun Process.output(): String = this.inputStream.bufferedReader().readText().trim('\n')
    }
}
