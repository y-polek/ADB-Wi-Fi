package dev.polek.adbwifi.settings

import com.intellij.openapi.components.service
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.ui.TextComponentAccessor
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.util.IconLoader
import com.intellij.openapi.util.SystemInfo
import com.intellij.ui.HyperlinkLabel
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import dev.polek.adbwifi.PluginBundle
import dev.polek.adbwifi.services.PropertiesService
import dev.polek.adbwifi.utils.AbstractMouseListener
import dev.polek.adbwifi.utils.GridBagLayoutPanel
import dev.polek.adbwifi.utils.isValidAdbLocation
import dev.polek.adbwifi.utils.panel
import java.awt.GridBagConstraints
import java.awt.Insets
import java.awt.event.MouseEvent
import java.io.File
import javax.swing.JComponent
import javax.swing.JTextField

class AdbWifiConfigurable : Configurable {

    private val properties = service<PropertiesService>()
    private lateinit var textField: TextFieldWithBrowseButton
    private lateinit var statusLabel: JBLabel
    private lateinit var defaultButton: HyperlinkLabel

    private val textComponentAccessor = object : TextComponentAccessor<JTextField> {
        override fun getText(component: JTextField) = component.text

        override fun setText(component: JTextField, text: String) {
            val file = File(text)
            val dirName = if (file.isFile) {
                file.parent.orEmpty()
            } else {
                text
            }
            component.text = dirName
            verifyAdbLocation()
        }
    }

    override fun getDisplayName(): String {
        return PluginBundle.getMessage("settingsPageName")
    }

    override fun createComponent(): JComponent? {
        val panel = GridBagLayoutPanel()

        val titleLabel = JBLabel(PluginBundle.message("adbLocationTitle"))
        panel.add(
            titleLabel,
            GridBagConstraints().apply {
                gridx = 0
                gridy = 0
                insets = Insets(0, 0, 0, 8)
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
            textComponentAccessor
        )
        panel.add(
            textField,
            GridBagConstraints().apply {
                gridx = 1
                gridy = 0
                gridwidth = 2
                fill = GridBagConstraints.HORIZONTAL
                weightx = 1.0
            }
        )

        statusLabel = JBLabel("'adb' binary found.")
        statusLabel.icon = IconLoader.getIcon("AllIcons.General.InspectionsError")
        panel.add(
            statusLabel,
            GridBagConstraints().apply {
                gridx = 1
                gridy = 1
                fill = GridBagConstraints.HORIZONTAL
                weightx = 1.0
                insets = Insets(4, 0, 0, 0)
            }
        )

        defaultButton = HyperlinkLabel(PluginBundle.message("defaultAdbLocationButton"))
        defaultButton.addMouseListener(object : AbstractMouseListener() {
            override fun mouseClicked(e: MouseEvent) {
                setAdbLocationText(properties.defaultAdbLocation)
            }
        })
        panel.add(
            defaultButton,
            GridBagConstraints().apply {
                gridx = 2
                gridy = 1
                insets = Insets(4, 0, 0, 0)
            }
        )

        verifyAdbLocation()

        return panel(top = panel)
    }

    override fun isModified(): Boolean {
        return textField.text != properties.adbLocation
    }

    override fun apply() {
        properties.adbLocation = textField.text
    }

    override fun reset() {
        setAdbLocationText(properties.adbLocation)
    }

    private fun setAdbLocationText(location: String) {
        textField.text = location
        verifyAdbLocation()
    }

    private fun adbLocationChooserDescriptor(): FileChooserDescriptor = when {
        SystemInfo.isMac -> FileChooserDescriptorFactory.createSingleFileOrFolderDescriptor()
        else -> FileChooserDescriptorFactory.createSingleFolderDescriptor()
    }

    private fun showVerifiedMessage() {
        statusLabel.apply {
            icon = OK_ICON
            text = ADB_VERIFIED_MESSAGE
            foreground = JBColor.foreground()
        }

        defaultButton.isVisible = false
    }

    private fun showVerificationErrorMessage() {
        statusLabel.apply {
            statusLabel.icon = ERROR_ICON
            text = ADB_VERIFICATION_ERROR_MESSAGE
            foreground = JBColor.RED
        }

        defaultButton.isVisible = true
    }

    private fun verifyAdbLocation() {
        val dir = textField.text
        if (isValidAdbLocation(dir)) {
            showVerifiedMessage()
        } else {
            showVerificationErrorMessage()
        }
    }

    private companion object {
        private val OK_ICON = IconLoader.getIcon("AllIcons.General.InspectionsOK")
        private val ERROR_ICON = IconLoader.getIcon("AllIcons.General.Error")
        private val ADB_VERIFIED_MESSAGE = PluginBundle.message("adbLocationVerifiedMessage")
        private val ADB_VERIFICATION_ERROR_MESSAGE = PluginBundle.message("adbLocationVerificationErrorMessage")
    }
}
