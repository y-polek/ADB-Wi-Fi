package dev.polek.adbwifi.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.project.DumbAware
import dev.polek.adbwifi.services.LogService

class ExpandLogAction : AnAction(), DumbAware {

    private val logService by lazy { service<LogService>() }

    override fun actionPerformed(e: AnActionEvent) {
        logService.setLogVisible(true)
    }
}
