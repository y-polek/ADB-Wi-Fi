package dev.polek.adbwifi.commandexecutor

data class CmdResult(val exitCode: Int, val output: String) {
    val isError: Boolean = exitCode != 0
}
