package dev.polek.adbwifi.adb

import dev.polek.adbwifi.commandexecutor.CommandExecutor

abstract class MockCommandExecutor : CommandExecutor {

    override fun exec(command: String, vararg envp: String): Sequence<String> {
        return mockOutput(command).splitToSequence('\n')
    }

    override suspend fun execAsync(command: String, vararg envp: String): String {
        return mockOutput(command)
    }

    override fun textExec(command: String, vararg envp: String): Boolean = true

    abstract fun mockOutput(command: String): String
}
