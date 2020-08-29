package dev.polek.adbwifi.utils

import com.intellij.openapi.util.SystemInfo
import java.io.File

fun isValidAdbLocation(dirPath: String): Boolean {
    val adbFileName = when {
        SystemInfo.isWindows -> "adb.exe"
        else -> "adb"
    }
    return File("$dirPath/$adbFileName").isFile
}
