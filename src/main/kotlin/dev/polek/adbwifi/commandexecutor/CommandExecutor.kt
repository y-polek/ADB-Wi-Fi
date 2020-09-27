package dev.polek.adbwifi.commandexecutor

import java.io.IOException

interface CommandExecutor {

    @Throws(IOException::class)
    fun exec(command: String, vararg envp: String): Sequence<String>

    @Throws(IOException::class)
    fun execSilently(command: String, vararg envp: String)

    @Throws(IOException::class)
    suspend fun execAsync(command: String, vararg envp: String): String

    @Throws(IOException::class)
    fun testExec(command: String, vararg envp: String): Boolean
}
