package dev.polek.adbwifi.model

/**
 * Data class representing the export file format.
 */
data class AdbCommandsExportFile(
    val version: Int = 1,
    val commands: List<AdbCommandConfig>
)
