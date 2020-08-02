package dev.polek.adbwifi.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.project.DumbAware
import dev.polek.adbwifi.services.AdbService

class RefreshAction : AnAction(), DumbAware {

    private val adbService by lazy { ServiceManager.getService(AdbService::class.java) }

    override fun actionPerformed(e: AnActionEvent) {
        adbService.refreshDeviceList()
    }
}
