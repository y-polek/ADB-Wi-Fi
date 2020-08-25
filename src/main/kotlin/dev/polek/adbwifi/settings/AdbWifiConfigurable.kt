package dev.polek.adbwifi.settings

import com.intellij.openapi.components.service
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.ui.TextComponentAccessor
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.util.SystemInfo
import com.intellij.ui.components.JBLabel
import dev.polek.adbwifi.MyBundle
import dev.polek.adbwifi.services.PropertiesService
import dev.polek.adbwifi.utils.GridBagLayoutPanel
import dev.polek.adbwifi.utils.panel
import java.awt.GridBagConstraints
import java.awt.Insets
import java.io.File
import javax.swing.JComponent
import javax.swing.JTextField

class AdbWifiConfigurable : Configurable {

    private val properties = service<PropertiesService>()
    private lateinit var textField: TextFieldWithBrowseButton

    override fun getDisplayName(): String {
        return MyBundle.getMessage("settingsPageName")
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
        textField.addBrowseFolderListener(
            null,
            null,
            null,
            adbLocationChooserDescriptor(),
            DIRECTORY_TEXT_COMPONENT_ACCESSOR
        )
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

    override fun reset() {
        textField.text = properties.adbLocation
    }

    private fun adbLocationChooserDescriptor(): FileChooserDescriptor = when {
        SystemInfo.isMac -> FileChooserDescriptorFactory.createSingleFileOrFolderDescriptor()
        else -> FileChooserDescriptorFactory.createSingleFolderDescriptor()
    }

    private companion object {

        private val DIRECTORY_TEXT_COMPONENT_ACCESSOR = object : TextComponentAccessor<JTextField> {
            override fun getText(component: JTextField) = component.text

            override fun setText(component: JTextField, text: String) {
                val file = File(text)
                val dirName = if (file.isFile) {
                    file.parent.orEmpty()
                } else {
                    text
                }
                component.text = dirName
            }
        }
    }
}
