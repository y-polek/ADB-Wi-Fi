package dev.polek.adbwifi.utils

import com.intellij.openapi.util.SystemInfo
import com.intellij.util.EnvironmentUtil
import java.io.File

private val SYSTEM_PATH: String
    get() = EnvironmentUtil.getEnvironmentMap()["PATH"] ?: System.getenv("PATH") ?: ""

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
        SYSTEM_PATH.splitToSequence(File.pathSeparatorChar)
    }

    return dirs
        .map { it.removeSuffix("/") }
        .any { dir ->
            File("$dir/$execFileName").isFile
        }
}

private fun exec(dir: String, execName: String): String = "${dir.removeSuffix("/")}/$execName"

private fun findExecInSystemPath(execName: String): String? {
    val file = SYSTEM_PATH.splitToSequence(File.pathSeparatorChar)
        .map { it.removeSuffix("/") }
        .map { dir ->
            File("$dir/$execName")
        }
        .firstOrNull(File::isFile)

    return file?.absolutePath
}

fun isValidAdbLocation(dirPath: String): Boolean = isValidExecPath(dirPath, "adb")
fun hasAdbInSystemPath(): Boolean = isValidExecPath(null, "adb")
fun adbExec(dir: String): String = exec(dir, "adb")
fun findAdbExecInSystemPath(): String? = findExecInSystemPath("adb")

fun isValidScrcpyLocation(dirPath: String): Boolean = isValidExecPath(dirPath, "scrcpy")
fun hasScrcpyInSystemPath(): Boolean = isValidExecPath(null, "scrcpy")
fun scrcpyExec(dir: String): String = exec(dir, "scrcpy")
fun findScrcpyExecInSystemPath(): String? = findExecInSystemPath("scrcpy")
