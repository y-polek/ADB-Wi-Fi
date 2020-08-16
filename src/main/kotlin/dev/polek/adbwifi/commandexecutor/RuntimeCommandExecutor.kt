package dev.polek.adbwifi.commandexecutor

import java.io.BufferedReader
import java.io.InputStreamReader

class RuntimeCommandExecutor : CommandExecutor {

    private val runtime = Runtime.getRuntime()

    override fun exec(command: String): Sequence<String> {
        val process = runtime.exec(command)
        return BufferedReader(InputStreamReader(process.inputStream)).lineSequence()
    }
}
