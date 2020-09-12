package dev.polek.adbwifi.utils

import com.intellij.openapi.util.SystemInfo
import java.io.File

private fun isValidExecPath(dirPath: String, execName: String): Boolean {
    val execFileName = when {
        SystemInfo.isWindows -> "$execName.exe"
        else -> execName
    }
    return File("$dirPath/$execFileName").isFile
}

fun isValidAdbLocation(dirPath: String): Boolean = isValidExecPath(dirPath, "adb")

fun isValidScrcpyLocation(dirPath: String): Boolean = isValidExecPath(dirPath, "scrcpy")
