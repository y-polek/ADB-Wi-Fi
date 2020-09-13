package dev.polek.adbwifi.utils

import com.intellij.openapi.util.SystemInfo
import java.io.File

/**
 * @param dirPath directory to lookup executable in, or `null` if executable should be looked up in system PATH
 */
private fun isValidExecPath(dirPath: String?, execName: String): Boolean {
    val execFileName = when {
        SystemInfo.isWindows -> "$execName.exe"
        else -> execName
    }

    val dirs = if (dirPath != null) {
        sequenceOf(dirPath)
    } else {
        System.getenv("PATH").splitToSequence(File.pathSeparatorChar)
    }

    return dirs.any { pathDir ->
        File("$pathDir/$execFileName").isFile
    }
}

fun isValidAdbLocation(dirPath: String): Boolean = isValidExecPath(dirPath, "adb")

fun hasAdbInSystemPath(): Boolean = isValidExecPath(null, "adb")

fun isValidScrcpyLocation(dirPath: String): Boolean = isValidExecPath(dirPath, "scrcpy")

fun hasScrcpyInSystemPath(): Boolean = isValidExecPath(null, "scrcpy")
