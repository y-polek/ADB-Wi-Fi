package dev.polek.adbwifi.commandexecutor

interface CommandExecutor {
    fun exec(command: String): Sequence<String>
    suspend fun execAsync(command: String): String
    fun textExec(command: String): Boolean
}
