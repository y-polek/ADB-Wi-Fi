package dev.polek.adbwifi.settings

import com.intellij.openapi.components.service
import com.intellij.openapi.options.Configurable
import dev.polek.adbwifi.PluginBundle
import dev.polek.adbwifi.services.AdbCommandsService
import java.awt.BorderLayout
import javax.swing.JComponent
import javax.swing.JPanel

class AdbCommandsConfigurable : Configurable {

    private val commandsService = service<AdbCommandsService>()

    private lateinit var commandsPanel: AdbCommandsSettingsPanel

    override fun getDisplayName(): String = PluginBundle.message("adbCommandsSettingsTitle")

    override fun createComponent(): JComponent {
        commandsPanel = AdbCommandsSettingsPanel()
        commandsPanel.loadFromService(commandsService)

        return JPanel(BorderLayout()).apply {
            add(commandsPanel, BorderLayout.CENTER)
        }
    }

    override fun isModified(): Boolean = commandsPanel.isModified(commandsService)

    override fun apply() {
        commandsPanel.applyToService(commandsService)
    }

    override fun reset() {
        commandsPanel.loadFromService(commandsService)
    }
}
