package dev.polek.adbwifi.actions

import com.intellij.notification.Notification
import com.intellij.notification.NotificationAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.options.ShowSettingsUtil
import dev.polek.adbwifi.PluginBundle

class OpenSettingsNotificationAction : NotificationAction(PluginBundle.message("goToSettingsButton")) {

    override fun actionPerformed(e: AnActionEvent, notification: Notification) {
        ShowSettingsUtil.getInstance().showSettingsDialog(null, PluginBundle.message("settingsPageName"))
    }
}
