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
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import dev.polek.adbwifi.PluginBundle
import dev.polek.adbwifi.services.PropertiesService
import dev.polek.adbwifi.utils.*
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

    private lateinit var scrcpyLocationField: TextFieldWithBrowseButton
    private lateinit var scrcpyStatusLabel: JBLabel

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
                insets = Insets(0, 0, GROUP_VERTICAL_INSET, 0)
            }
        )

        val adbSystemPathCheckbox = JBCheckBox(PluginBundle.message("adbUseSystemPath"))
        panel.add(
            adbSystemPathCheckbox,
            GridBagConstraints().apply {
                gridx = 0
                gridy = 1
                gridwidth = 3
                anchor = GridBagConstraints.LINE_START
                insets = Insets(0, GROUP_LEFT_INSET, 4, 0)
            }
        )

        val adbLocationTitle = JBLabel(PluginBundle.message("adbPathTitle"))
        panel.add(
            adbLocationTitle,
            GridBagConstraints().apply {
                gridx = 0
                gridy = 2
                gridwidth = 1
                anchor = GridBagConstraints.LINE_START
                insets = Insets(0, GROUP_LEFT_INSET, 0, 8)
            }
        )

        adbLocationField = TextFieldWithBrowseButton()
        adbLocationField.text = properties.adbLocation
        adbLocationField.isEditable = false
        adbLocationField.addBrowseFolderListener(
            null,
            null,
            null,
            executableChooserDescriptor(),
            ExecutablePathTextComponentAccessor(::verifyAdbLocation)
        )
        panel.add(
            adbLocationField,
            GridBagConstraints().apply {
                gridx = 1
                gridy = 2
                gridwidth = 2
                fill = GridBagConstraints.HORIZONTAL
                weightx = 1.0
            }
        )

        adbStatusLabel = JBLabel()
        adbStatusLabel.icon = IconLoader.getIcon("AllIcons.General.InspectionsError")
        panel.add(
            adbStatusLabel,
            GridBagConstraints().apply {
                gridx = 1
                gridy = 3
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
                gridy = 3
                gridwidth = 1
                insets = Insets(4, 0, 0, 0)
            }
        )

        val scrcpySeparator = TitledSeparator(PluginBundle.message("scrcpySettingsTitle"))
        panel.add(
            scrcpySeparator,
            GridBagConstraints().apply {
                gridx = 0
                gridy = 4
                gridwidth = 3
                fill = GridBagConstraints.HORIZONTAL
                weightx = 1.0
                insets = Insets(GROUP_VERTICAL_INSET, 0, GROUP_VERTICAL_INSET, 0)
            }
        )

        val scrcpySystemPathCheckbox = JBCheckBox(PluginBundle.message("scrcpyUseSystemPath"))
        panel.add(
            scrcpySystemPathCheckbox,
            GridBagConstraints().apply {
                gridx = 0
                gridy = 5
                gridwidth = 3
                anchor = GridBagConstraints.LINE_START
                insets = Insets(0, GROUP_LEFT_INSET, 4, 0)
            }
        )

        val scrcpyPathTitle = JBLabel(PluginBundle.message("scrcpyPathTitle"))
        panel.add(
            scrcpyPathTitle,
            GridBagConstraints().apply {
                gridx = 0
                gridy = 6
                gridwidth = 1
                anchor = GridBagConstraints.LINE_START
                insets = Insets(0, GROUP_LEFT_INSET, 0, 8)
            }
        )

        scrcpyLocationField = TextFieldWithBrowseButton()
        scrcpyLocationField.text = properties.scrcpyLocation
        scrcpyLocationField.isEditable = false
        scrcpyLocationField.addBrowseFolderListener(
            null,
            null,
            null,
            executableChooserDescriptor(),
            ExecutablePathTextComponentAccessor(::verifyScrcpyLocation)
        )
        panel.add(
            scrcpyLocationField,
            GridBagConstraints().apply {
                gridx = 1
                gridy = 6
                gridwidth = 2
                fill = GridBagConstraints.HORIZONTAL
                weightx = 1.0
            }
        )

        scrcpyStatusLabel = JBLabel()
        scrcpyStatusLabel.icon = IconLoader.getIcon("AllIcons.General.InspectionsError")
        panel.add(
            scrcpyStatusLabel,
            GridBagConstraints().apply {
                gridx = 1
                gridy = 7
                gridwidth = 1
                fill = GridBagConstraints.HORIZONTAL
                weightx = 1.0
                insets = Insets(4, 0, 0, 0)
            }
        )

        verifyAdbLocation()
        verifyScrcpyLocation()

        return panel(top = panel)
    }

    override fun isModified(): Boolean {
        return adbLocationField.text != properties.adbLocation || scrcpyLocationField.text != properties.scrcpyLocation
    }

    override fun apply() {
        properties.adbLocation = adbLocationField.text
        properties.scrcpyLocation = scrcpyLocationField.text
    }

    override fun reset() {
        setAdbLocationText(properties.adbLocation)
        setScrcpyLocationText(properties.scrcpyLocation)
    }

    private fun setAdbLocationText(location: String) {
        adbLocationField.text = location
        verifyAdbLocation()
    }

    private fun setScrcpyLocationText(location: String) {
        scrcpyLocationField.text = location
        verifyScrcpyLocation()
    }

    private fun executableChooserDescriptor(): FileChooserDescriptor = when {
        SystemInfo.isMac -> FileChooserDescriptorFactory.createSingleFileOrFolderDescriptor()
        else -> FileChooserDescriptorFactory.createSingleFolderDescriptor()
    }

    private fun showAdbVerifiedMessage() {
        adbStatusLabel.apply {
            icon = OK_ICON
            text = VERIFIED_MESSAGE
            foreground = JBColor.foreground()
        }

        defaultAdbLocationButton.isVisible = false
    }

    private fun showScrcpyVerifiedMessage() {
        scrcpyStatusLabel.apply {
            icon = OK_ICON
            text = VERIFIED_MESSAGE
            foreground = JBColor.foreground()
        }
    }

    private fun showAdbVerificationErrorMessage() {
        adbStatusLabel.apply {
            icon = ERROR_ICON
            text = ADB_VERIFICATION_ERROR_MESSAGE
            foreground = JBColor.RED
        }

        defaultAdbLocationButton.isVisible = true
    }

    private fun showScrcpyVerificationErrorMessage() {
        scrcpyStatusLabel.apply {
            icon = ERROR_ICON
            text = SCRCPY_VERIFICATION_ERROR_MESSAGE
            foreground = JBColor.RED
        }
    }

    private fun verifyAdbLocation() {
        val dir = adbLocationField.text
        if (isValidAdbLocation(dir)) {
            showAdbVerifiedMessage()
        } else {
            showAdbVerificationErrorMessage()
        }
    }

    private fun verifyScrcpyLocation() {
        val dir = scrcpyLocationField.text
        if (isValidScrcpyLocation(dir)) {
            showScrcpyVerifiedMessage()
        } else {
            showScrcpyVerificationErrorMessage()
        }
    }

    private class ExecutablePathTextComponentAccessor(
        val onTextChanged: () -> Unit
    ) : TextComponentAccessor<JTextField> {

        override fun getText(component: JTextField): String = component.text

        override fun setText(component: JTextField, text: String) {
            val file = File(text)
            val dirName = if (file.isFile) {
                file.parent.orEmpty()
            } else {
                text
            }
            component.text = dirName

            onTextChanged.invoke()
        }
    }

    private companion object {
        private const val GROUP_VERTICAL_INSET = 10
        private const val GROUP_LEFT_INSET = 20
        private val OK_ICON = IconLoader.getIcon("AllIcons.General.InspectionsOK")
        private val ERROR_ICON = IconLoader.getIcon("AllIcons.General.Error")
        private val VERIFIED_MESSAGE = PluginBundle.message("adbPathVerifiedMessage")
        private val ADB_VERIFICATION_ERROR_MESSAGE = PluginBundle.message("adbPathVerificationErrorMessage")
        private val SCRCPY_VERIFICATION_ERROR_MESSAGE = PluginBundle.message("scrcpyPathVerificationErrorMessage")
    }
}
