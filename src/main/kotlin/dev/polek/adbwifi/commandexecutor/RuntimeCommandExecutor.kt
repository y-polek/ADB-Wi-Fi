package dev.polek.adbwifi.commandexecutor

import dev.polek.adbwifi.LOG
import kotlinx.coroutines.delay
import java.io.IOException
import java.lang.StringBuilder

class RuntimeCommandExecutor : CommandExecutor {

    private val runtime = Runtime.getRuntime()

    override fun exec(command: String, vararg envp: String): String {
        val process = runtime.exec(command, envp)
        val output = process.output()
        log {
            appendln("exec> $command ${envp.joinToString()}")
            appendln(output)
        }
        return output
    }

    override fun execSilently(command: String, vararg envp: String) {
        val process = runtime.exec(command, envp)
        log {
            appendln("execSilently> $command ${envp.joinToString()}")
            appendln(process.output())
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun execAsync(command: String, vararg envp: String): String {
        val process = runtime.exec(command, envp)
        while (process.isAlive) {
            delay(500L)
        }
        val output = process.output()
        log {
            appendln("execAsync> $command ${envp.joinToString()}")
            appendln(output)
        }
        return output
    }

    override fun testExec(command: String, vararg envp: String): Boolean {
        val process = try {
            runtime.exec(command, envp)
        } catch (e: IOException) {
            return false
        }
        process.waitFor()
        val exitValue = process.exitValue()
        log {
            val output = process.output()
            appendln("testExec> $command ${envp.joinToString()}")
            appendln(output)
            appendln("-> $exitValue")
        }
        return exitValue == 0
    }

    private companion object {

        private const val DEBUG_ENABLED = false

        private inline fun log(buildMessage: StringBuilder.() -> Unit) {
            if (DEBUG_ENABLED) {
                val text = buildString {
                    appendln("RuntimeCommandExecutor")
                    this.buildMessage()
                }
                LOG.warn(text)
            }
        }

        private fun Process.output(): String = this.inputStream.bufferedReader().readText().trim('\n')
    }
}
