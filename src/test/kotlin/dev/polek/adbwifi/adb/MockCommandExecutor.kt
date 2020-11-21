package dev.polek.adbwifi.adb

import dev.polek.adbwifi.commandexecutor.CommandExecutor
import dev.polek.adbwifi.utils.adbExec

abstract class MockCommandExecutor(
    adbLocation: String
) : CommandExecutor {

    val adb = adbExec(adbLocation)

    override fun exec(command: String, vararg envp: String): Sequence<String> {
        return mockOutput(command).lineSequence()
    }

    override suspend fun execAsync(command: String, vararg envp: String): String {
        return mockOutput(command)
    }

    override fun testExec(command: String, vararg envp: String): Boolean = true

    abstract fun mockOutput(command: String): String

    companion object {
        operator fun invoke(
            adbLocation: String,
            output: MockCommandExecutor.(command: String) -> String
        ) = object : MockCommandExecutor(adbLocation) {
            override fun mockOutput(command: String) = this.output(command)
        }
    }
}
