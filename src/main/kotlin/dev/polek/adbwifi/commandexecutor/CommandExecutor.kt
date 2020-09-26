package dev.polek.adbwifi.commandexecutor

interface CommandExecutor {
    fun exec(command: String, vararg envp: String): Sequence<String>
    suspend fun execAsync(command: String, vararg envp: String): String
    fun textExec(command: String, vararg envp: String): Boolean
}
