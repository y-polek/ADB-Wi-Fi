package dev.polek.adbwifi.services

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil
import com.intellij.util.xmlb.annotations.OptionTag
import dev.polek.adbwifi.model.AdbCommandConfig
import dev.polek.adbwifi.model.AdbCommandConfigListConverter
import java.util.UUID

@State(
    name = "AdbCommandsService",
    storages = [Storage("adbWifiCommands.xml")]
)
class AdbCommandsService : PersistentStateComponent<AdbCommandsService> {

    @OptionTag(converter = AdbCommandConfigListConverter::class)
    var commands: List<AdbCommandConfig> = defaultCommands()

    fun getEnabledCommands(): List<AdbCommandConfig> =
        commands.filter { it.isEnabled }.sortedBy { it.order }

    fun updateCommand(config: AdbCommandConfig) {
        commands = commands.map { if (it.id == config.id) config else it }
    }

    fun addCustomCommand(name: String, command: String, iconId: String): AdbCommandConfig {
        val maxOrder = commands.maxOfOrNull { it.order } ?: -1
        val newCommand = AdbCommandConfig(
            id = UUID.randomUUID().toString(),
            name = name,
            command = command,
            iconId = iconId,
            isBuiltIn = false,
            isEnabled = true,
            order = maxOrder + 1
        )
        commands = commands + newCommand
        return newCommand
    }

    fun removeCustomCommand(id: String) {
        commands = commands.filter { it.id != id || it.isBuiltIn }
    }

    fun moveUp(id: String) {
        val sorted = commands.sortedBy { it.order }
        val index = sorted.indexOfFirst { it.id == id }
        if (index <= 0) return

        val current = sorted[index]
        val above = sorted[index - 1]
        commands = commands.map { cmd ->
            when (cmd.id) {
                current.id -> cmd.copy(order = above.order)
                above.id -> cmd.copy(order = current.order)
                else -> cmd
            }
        }
    }

    fun moveDown(id: String) {
        val sorted = commands.sortedBy { it.order }
        val index = sorted.indexOfFirst { it.id == id }
        if (index < 0 || index >= sorted.lastIndex) return

        val current = sorted[index]
        val below = sorted[index + 1]
        commands = commands.map { cmd ->
            when (cmd.id) {
                current.id -> cmd.copy(order = below.order)
                below.id -> cmd.copy(order = current.order)
                else -> cmd
            }
        }
    }

    fun resetToDefaults() {
        commands = defaultCommands()
    }

    override fun getState() = this

    override fun loadState(state: AdbCommandsService) {
        XmlSerializerUtil.copyBean(state, this)
    }

    companion object {
        private const val KILL_APP_ID = "KILL_APP"
        private const val START_APP_ID = "START_APP"
        private const val RESTART_APP_ID = "RESTART_APP"
        private const val CLEAR_DATA_ID = "CLEAR_DATA"
        private const val CLEAR_DATA_AND_RESTART_ID = "CLEAR_DATA_AND_RESTART"

        fun defaultCommands(): List<AdbCommandConfig> = listOf(
            AdbCommandConfig(
                id = KILL_APP_ID,
                name = "Kill app",
                command = "am force-stop {package}",
                iconId = "suspend",
                isBuiltIn = true,
                isEnabled = true,
                order = 0
            ),
            AdbCommandConfig(
                id = START_APP_ID,
                name = "Start app",
                command = "monkey -p {package} -c android.intent.category.LAUNCHER 1",
                iconId = "execute",
                isBuiltIn = true,
                isEnabled = true,
                order = 1
            ),
            AdbCommandConfig(
                id = RESTART_APP_ID,
                name = "Restart app",
                command = "am force-stop {package}\nmonkey -p {package} -c android.intent.category.LAUNCHER 1",
                iconId = "restart",
                isBuiltIn = true,
                isEnabled = true,
                order = 2
            ),
            AdbCommandConfig(
                id = CLEAR_DATA_ID,
                name = "Clear app data",
                command = "pm clear {package}",
                iconId = "clear",
                isBuiltIn = true,
                isEnabled = true,
                order = 3
            ),
            AdbCommandConfig(
                id = CLEAR_DATA_AND_RESTART_ID,
                name = "Clear app data and restart",
                command = "pm clear {package}\nmonkey -p {package} -c android.intent.category.LAUNCHER 1",
                iconId = "forceRefresh",
                isBuiltIn = true,
                isEnabled = true,
                order = 4
            )
        )
    }
}
