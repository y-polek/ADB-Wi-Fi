package dev.polek.adbwifi.commandexecutor

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

class RuntimeCommandExecutor : CommandExecutor {

    private val runtime = Runtime.getRuntime()

    override fun exec(command: String): Sequence<String> {
        val process = runtime.exec(command)
        return BufferedReader(InputStreamReader(process.inputStream)).lineSequence()
    }

    override fun textExec(command: String): Boolean {
        val process = try {
            runtime.exec(command)
        } catch (e: IOException) {
            return false
        }
        process.waitFor()
        return process.exitValue() == 0
    }
}
