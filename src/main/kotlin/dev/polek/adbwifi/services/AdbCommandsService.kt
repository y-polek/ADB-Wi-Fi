package dev.polek.adbwifi.services

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil
import com.intellij.util.xmlb.annotations.OptionTag
import dev.polek.adbwifi.model.AdbCommandConfig
import dev.polek.adbwifi.model.AdbCommandConfigListConverter

@State(
    name = "AdbCommandsService",
    storages = [Storage("adbWifiCommands.xml")]
)
class AdbCommandsService : PersistentStateComponent<AdbCommandsService> {

    @OptionTag(converter = AdbCommandConfigListConverter::class)
    var commands: List<AdbCommandConfig> = defaultCommands()

    fun getEnabledCommands(): List<AdbCommandConfig> =
        commands.filter { it.isEnabled }.sortedBy { it.order }

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
        private const val UNINSTALL_APP_ID = "UNINSTALL_APP"
        private const val UNINSTALL_APP_KEEP_DATA_ID = "UNINSTALL_APP_KEEP_DATA"

        fun defaultCommands(): List<AdbCommandConfig> = listOf(
            AdbCommandConfig(
                id = KILL_APP_ID,
                name = "Kill app",
                command = "am force-stop {package}",
                iconId = "stop",
                isEnabled = true,
                order = 0
            ),
            AdbCommandConfig(
                id = START_APP_ID,
                name = "Start app",
                command = "monkey -p {package} -c android.intent.category.LAUNCHER 1",
                iconId = "play",
                isEnabled = true,
                order = 1
            ),
            AdbCommandConfig(
                id = RESTART_APP_ID,
                name = "Restart app",
                command = "am force-stop {package}\nmonkey -p {package} -c android.intent.category.LAUNCHER 1",
                iconId = "restart",
                isEnabled = true,
                order = 2
            ),
            AdbCommandConfig(
                id = CLEAR_DATA_ID,
                name = "Clear app data",
                command = "pm clear {package}",
                iconId = "clear",
                isEnabled = true,
                order = 3
            ),
            AdbCommandConfig(
                id = CLEAR_DATA_AND_RESTART_ID,
                name = "Clear app data and restart",
                command = "pm clear {package}\nmonkey -p {package} -c android.intent.category.LAUNCHER 1",
                iconId = "force-refresh",
                isEnabled = true,
                order = 4
            ),
            AdbCommandConfig(
                id = UNINSTALL_APP_ID,
                name = "Uninstall app",
                command = "pm uninstall {package}",
                iconId = "trash",
                isEnabled = false,
                order = 5
            ),
            AdbCommandConfig(
                id = UNINSTALL_APP_KEEP_DATA_ID,
                name = "Uninstall app (keep data)",
                command = "pm uninstall -k {package}",
                iconId = "delete",
                isEnabled = false,
                order = 6
            )
        )
    }
}
