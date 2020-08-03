package dev.polek.adbwifi.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ToggleAction
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.project.DumbAware
import dev.polek.adbwifi.services.ShellService

class ShellAction : ToggleAction(), DumbAware {

    private val service by lazy { ServiceManager.getService(ShellService::class.java) }

    override fun isSelected(e: AnActionEvent) = service.isShellVisible

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        service.isShellVisible = state
    }
}
