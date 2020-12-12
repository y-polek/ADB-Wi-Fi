package dev.polek.adbwifi.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAware
import dev.polek.adbwifi.ui.view.ConnectDeviceDialogWrapper

class ConnectDeviceAction : AnAction(), DumbAware {

    override fun actionPerformed(e: AnActionEvent) {
        ConnectDeviceDialogWrapper().show()
    }
}
