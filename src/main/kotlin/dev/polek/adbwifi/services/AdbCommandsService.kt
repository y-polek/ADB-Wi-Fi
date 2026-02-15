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
        @Suppress("LongMethod")
        fun defaultCommands(): List<AdbCommandConfig> = listOf(
            AdbCommandConfig(
                name = "Kill app",
                command = "shell am force-stop {package}",
                iconId = "square",
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
                iconId = "rotate-ccw",
                isEnabled = true,
                order = 2
            ),
            AdbCommandConfig(
                name = "Clear app data",
                command = "shell pm clear {package}",
                iconId = "trash",
                isEnabled = true,
                order = 3,
                requiresConfirmation = true
            ),
            AdbCommandConfig(
                name = "Clear app data and restart",
                command = "shell pm clear {package}\n" +
                    "shell monkey -p {package} -c android.intent.category.LAUNCHER 1",
                iconId = "refresh-cw",
                isEnabled = true,
                order = 4,
                requiresConfirmation = true
            ),
            AdbCommandConfig(
                name = "Uninstall app",
                command = "shell pm uninstall {package}",
                iconId = "trash",
                iconColor = "red",
                isEnabled = true,
                order = 5,
                requiresConfirmation = true
            ),
            AdbCommandConfig(
                name = "Uninstall app (keep data)",
                command = "shell pm uninstall -k {package}",
                iconId = "trash",
                isEnabled = true,
                order = 6,
                requiresConfirmation = true
            ),
            AdbCommandConfig(
                name = "Install APK",
                command = "install {file .apk}",
                iconId = "download",
                isEnabled = true,
                order = 7
            ),
            AdbCommandConfig(
                name = "Reboot device",
                command = "reboot",
                iconId = "power",
                iconColor = "orange",
                isEnabled = true,
                order = 8,
                requiresConfirmation = true
            ),
            AdbCommandConfig(
                name = "Simulate incoming SMS (emulator only)",
                command = "emu sms send ADB-Wi-Fi {param Message}",
                iconId = "message-circle",
                isEnabled = true,
                order = 9
            ),
            AdbCommandConfig(
                name = "Enable TalkBack (emulator only)",
                command = "shell settings put secure enabled_accessibility_services " +
                    "com.google.android.marvin.talkback/com.google.android.marvin.talkback.TalkBackService",
                iconId = "eye",
                isEnabled = true,
                order = 10
            ),
            AdbCommandConfig(
                name = "Disable TalkBack",
                command = "shell settings put secure enabled_accessibility_services \"\"",
                iconId = "eye-off",
                isEnabled = true,
                order = 11
            ),
            AdbCommandConfig(
                name = "Show touches",
                command = "shell settings put system show_touches 1",
                iconId = "mouse-pointer-click",
                isEnabled = true,
                order = 12
            ),
            AdbCommandConfig(
                name = "Hide touches",
                command = "shell settings put system show_touches 0",
                iconId = "mouse-pointer-click",
                isEnabled = true,
                order = 13
            ),
            AdbCommandConfig(
                name = "Grant all permissions",
                command = "shell dumpsys package {package} | grep \"android.permission\" | " +
                    "grep \"granted=false\" | cut -d: -f1 | tr -d ' ' | " +
                    "while read perm; do pm grant {package} \"\$perm\"; done",
                iconId = "shield",
                isEnabled = true,
                order = 14
            ),
            AdbCommandConfig(
                name = "Revoke all permissions",
                command = "shell dumpsys package {package} | grep \"android.permission\" | " +
                    "grep \"granted=true\" | cut -d: -f1 | tr -d ' ' | " +
                    "while read perm; do pm revoke {package} \"\$perm\"; done",
                iconId = "shield-off",
                isEnabled = true,
                order = 15
            ),
            AdbCommandConfig(
                name = "Light theme",
                command = "shell cmd uimode night no",
                iconId = "sun",
                isEnabled = true,
                order = 16
            ),
            AdbCommandConfig(
                name = "Dark theme",
                command = "shell cmd uimode night yes",
                iconId = "moon",
                isEnabled = true,
                order = 17
            ),
            AdbCommandConfig(
                name = "System theme",
                command = "shell cmd uimode night auto",
                iconId = "sun-moon",
                isEnabled = true,
                order = 18
            )
        )
    }
}
