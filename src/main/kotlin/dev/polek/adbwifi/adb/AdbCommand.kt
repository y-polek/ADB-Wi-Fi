package dev.polek.adbwifi.adb

enum class AdbCommand(val messageKey: String) {
    KILL_APP("adbCommandKillApp"),
    START_APP("adbCommandStartApp"),
    RESTART_APP("adbCommandRestartApp"),
    CLEAR_DATA("adbCommandClearData"),
    CLEAR_DATA_AND_RESTART("adbCommandClearDataRestart")
}
