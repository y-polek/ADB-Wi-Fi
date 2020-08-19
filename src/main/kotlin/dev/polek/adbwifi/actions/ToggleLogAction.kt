package dev.polek.adbwifi.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ToggleAction
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.project.DumbAware
import dev.polek.adbwifi.services.LogService

class ToggleLogAction : ToggleAction(), DumbAware {

    private val service by lazy { ServiceManager.getService(LogService::class.java) }

    override fun isSelected(e: AnActionEvent) = service.isLogVisible

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        service.isLogVisible = state
    }
}
