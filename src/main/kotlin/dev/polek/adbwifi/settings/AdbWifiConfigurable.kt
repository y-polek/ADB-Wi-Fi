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
import com.intellij.ui.TitledSeparator
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
    private lateinit var adbLocationField: TextFieldWithBrowseButton
    private lateinit var adbStatusLabel: JBLabel
    private lateinit var defaultAdbLocationButton: HyperlinkLabel

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
        return PluginBundle.message("settingsPageName")
    }

    override fun createComponent(): JComponent? {
        val panel = GridBagLayoutPanel()

        val adbSeparator = TitledSeparator(PluginBundle.message("adbSettingsTitle"))
        panel.add(
            adbSeparator,
            GridBagConstraints().apply {
                gridx = 0
                gridy = 0
                gridwidth = 3
                fill = GridBagConstraints.HORIZONTAL
                weightx = 1.0
            }
        )

        val adbLocationTitle = JBLabel(PluginBundle.message("adbLocationTitle"))
        panel.add(
            adbLocationTitle,
            GridBagConstraints().apply {
                gridx = 0
                gridy = 1
                gridwidth = 1
                insets = Insets(0, 20, 0, 8)
            }
        )

        adbLocationField = TextFieldWithBrowseButton()
        adbLocationField.text = properties.adbLocation
        adbLocationField.isEditable = false
        adbLocationField.addBrowseFolderListener(
            null,
            null,
            null,
            adbLocationChooserDescriptor(),
            textComponentAccessor
        )
        panel.add(
            adbLocationField,
            GridBagConstraints().apply {
                gridx = 1
                gridy = 1
                gridwidth = 2
                fill = GridBagConstraints.HORIZONTAL
                weightx = 1.0
            }
        )

        adbStatusLabel = JBLabel("'adb' binary found.")
        adbStatusLabel.icon = IconLoader.getIcon("AllIcons.General.InspectionsError")
        panel.add(
            adbStatusLabel,
            GridBagConstraints().apply {
                gridx = 1
                gridy = 2
                gridwidth = 1
                fill = GridBagConstraints.HORIZONTAL
                weightx = 1.0
                insets = Insets(4, 0, 0, 0)
            }
        )

        defaultAdbLocationButton = HyperlinkLabel(PluginBundle.message("defaultAdbLocationButton"))
        defaultAdbLocationButton.addMouseListener(object : AbstractMouseListener() {
            override fun mouseClicked(e: MouseEvent) {
                setAdbLocationText(properties.defaultAdbLocation)
            }
        })
        panel.add(
            defaultAdbLocationButton,
            GridBagConstraints().apply {
                gridx = 2
                gridy = 2
                gridwidth = 1
                insets = Insets(4, 0, 0, 0)
            }
        )

        val scrcpySeparator = TitledSeparator(PluginBundle.message("scrcpySettingsTitle"))
        panel.add(
            scrcpySeparator,
            GridBagConstraints().apply {
                gridx = 0
                gridy = 3
                gridwidth = 3
                fill = GridBagConstraints.HORIZONTAL
                weightx = 1.0
            }
        )

        verifyAdbLocation()

        return panel(top = panel)
    }

    override fun isModified(): Boolean {
        return adbLocationField.text != properties.adbLocation
    }

    override fun apply() {
        properties.adbLocation = adbLocationField.text
    }

    override fun reset() {
        setAdbLocationText(properties.adbLocation)
    }

    private fun setAdbLocationText(location: String) {
        adbLocationField.text = location
        verifyAdbLocation()
    }

    private fun adbLocationChooserDescriptor(): FileChooserDescriptor = when {
        SystemInfo.isMac -> FileChooserDescriptorFactory.createSingleFileOrFolderDescriptor()
        else -> FileChooserDescriptorFactory.createSingleFolderDescriptor()
    }

    private fun showVerifiedMessage() {
        adbStatusLabel.apply {
            icon = OK_ICON
            text = ADB_VERIFIED_MESSAGE
            foreground = JBColor.foreground()
        }

        defaultAdbLocationButton.isVisible = false
    }

    private fun showVerificationErrorMessage() {
        adbStatusLabel.apply {
            adbStatusLabel.icon = ERROR_ICON
            text = ADB_VERIFICATION_ERROR_MESSAGE
            foreground = JBColor.RED
        }

        defaultAdbLocationButton.isVisible = true
    }

    private fun verifyAdbLocation() {
        val dir = adbLocationField.text
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
