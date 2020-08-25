package dev.polek.adbwifi.settings

import com.intellij.openapi.components.service
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory.createSingleFolderDescriptor
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.components.JBLabel
import dev.polek.adbwifi.MyBundle
import dev.polek.adbwifi.services.PropertiesService
import dev.polek.adbwifi.utils.GridBagLayoutPanel
import dev.polek.adbwifi.utils.panel
import java.awt.GridBagConstraints
import java.awt.Insets
import javax.swing.JComponent

class AdbWifiConfigurable : Configurable {

    private val properties = service<PropertiesService>()
    private lateinit var textField: TextFieldWithBrowseButton

    override fun getDisplayName(): String {
        return MyBundle.getMessage("name")
    }

    override fun createComponent(): JComponent? {
        val panel = GridBagLayoutPanel()

        val titleLabel = JBLabel(MyBundle.message("adbLocationTitle"))
        panel.add(
            titleLabel,
            GridBagConstraints().apply {
                gridx = 0
                gridy = 0
            }
        )

        textField = TextFieldWithBrowseButton()
        textField.text = properties.adbLocation
        textField.isEditable = false
        textField.addBrowseFolderListener(null, null, null, createSingleFolderDescriptor())
        panel.add(
            textField,
            GridBagConstraints().apply {
                gridx = 1
                gridy = 0
                fill = GridBagConstraints.HORIZONTAL
                weightx = 1.0
                insets = Insets(0, 8, 0, 0)
            }
        )

        return panel(top = panel)
    }

    override fun isModified(): Boolean {
        return textField.text != properties.adbLocation
    }

    override fun apply() {
        properties.adbLocation = textField.text
    }
}
