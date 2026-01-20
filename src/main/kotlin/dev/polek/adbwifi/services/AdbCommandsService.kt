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
        fun defaultCommands(): List<AdbCommandConfig> = listOf(
            AdbCommandConfig(
                name = "Kill app",
                command = "shell am force-stop {package}",
                iconId = "stop",
                isEnabled = true,
                order = 0
            ),
            AdbCommandConfig(
                name = "Start app",
                command = "shell monkey -p {package} -c android.intent.category.LAUNCHER 1",
                iconId = "play",
                isEnabled = true,
                order = 1
            ),
            AdbCommandConfig(
                name = "Restart app",
                command = "shell am force-stop {package}\n" +
                    "shell monkey -p {package} -c android.intent.category.LAUNCHER 1",
                iconId = "restart",
                isEnabled = true,
                order = 2
            ),
            AdbCommandConfig(
                name = "Clear app data",
                command = "shell pm clear {package}",
                iconId = "clear-2",
                isEnabled = true,
                order = 3,
                requiresConfirmation = true
            ),
            AdbCommandConfig(
                name = "Clear app data and restart",
                command = "shell pm clear {package}\n" +
                    "shell monkey -p {package} -c android.intent.category.LAUNCHER 1",
                iconId = "refresh-2",
                isEnabled = true,
                order = 4,
                requiresConfirmation = true
            ),
            AdbCommandConfig(
                name = "Uninstall app",
                command = "shell pm uninstall {package}",
                iconId = "trash",
                isEnabled = true,
                order = 5,
                requiresConfirmation = true
            ),
            AdbCommandConfig(
                name = "Uninstall app (keep data)",
                command = "shell pm uninstall -k {package}",
                iconId = "delete",
                isEnabled = true,
                order = 6,
                requiresConfirmation = true
            ),
            AdbCommandConfig(
                name = "Reboot device",
                command = "reboot",
                iconId = "restart-2",
                isEnabled = true,
                order = 7,
                requiresConfirmation = true
            ),
            AdbCommandConfig(
                name = "Simulate incoming SMS (emulator only)",
                command = "emu sms send ADB-Wi-Fi {param Message}",
                iconId = "console",
                isEnabled = true,
                order = 8
            )
        )
    }
}
