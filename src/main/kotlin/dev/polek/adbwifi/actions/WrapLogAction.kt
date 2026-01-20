package dev.polek.adbwifi.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ToggleAction
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.DumbAware
import dev.polek.adbwifi.services.LogService

class WrapLogAction : ToggleAction(), DumbAware {

    private val service by lazy { ApplicationManager.getApplication().getService(LogService::class.java) }

    override fun isSelected(e: AnActionEvent) = service.isLogWrapContent.value

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        service.setLogWrapContent(state)
    }

    override fun getActionUpdateThread() = ActionUpdateThread.EDT
}
