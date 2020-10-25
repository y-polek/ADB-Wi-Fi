package dev.polek.adbwifi.settings

import com.intellij.ide.BrowserUtil
import com.intellij.openapi.components.service
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.ui.TextComponentAccessor
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.util.IconLoader
import com.intellij.openapi.util.SystemInfo
import com.intellij.ui.ContextHelpLabel
import com.intellij.ui.HyperlinkLabel
import com.intellij.ui.JBColor
import com.intellij.ui.TitledSeparator
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextArea
import com.intellij.util.ui.UIUtil
import dev.polek.adbwifi.PluginBundle
import dev.polek.adbwifi.SCRCPY_FEATURE_ENABLED
import dev.polek.adbwifi.services.PropertiesService
import dev.polek.adbwifi.utils.GridBagLayoutPanel
import dev.polek.adbwifi.utils.isValidAdbLocation
import dev.polek.adbwifi.utils.isValidScrcpyLocation
import dev.polek.adbwifi.utils.panel
import java.awt.GridBagConstraints
import java.awt.Insets
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.File
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JTextField
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

class AdbWifiConfigurable : Configurable {

    private val properties = service<PropertiesService>()

    private lateinit var adbSystemPathCheckbox: JBCheckBox
    private lateinit var adbLocationTitle: JBLabel
    private lateinit var adbLocationField: TextFieldWithBrowseButton
    private lateinit var adbStatusLabel: JBLabel
    private lateinit var defaultAdbLocationButton: HyperlinkLabel

    private lateinit var scrcpyEnabledCheckbox: JBCheckBox
    private lateinit var scrcpySystemPathCheckbox: JBCheckBox
    private lateinit var scrcpyLocationTitle: JBLabel
    private lateinit var scrcpyLocationField: TextFieldWithBrowseButton
    private lateinit var scrcpyStatusLabel: JBLabel

    override fun getDisplayName(): String {
        return PluginBundle.message("settingsPageName")
    }

    override fun createComponent(): JComponent? {
        val panel = GridBagLayoutPanel()

        createAdbSettings(panel)
        if (SCRCPY_FEATURE_ENABLED) {
            createScrcpySettings(panel)
        }

        verifyAdbLocation()
        updateAdbLocationSettingsState()

        if (SCRCPY_FEATURE_ENABLED) {
            verifyScrcpyLocation()
            updateScrcpySettingsState()
        }

        return panel(top = panel)
    }

    private fun createAdbSettings(panel: JPanel) {
        val separator = TitledSeparator(PluginBundle.message("adbSettingsTitle"))
        panel.add(
            separator,
            GridBagConstraints().apply {
                gridx = 0
                gridy = 0
                gridwidth = 3
                fill = GridBagConstraints.HORIZONTAL
                weightx = 1.0
                insets = Insets(0, 0, GROUP_VERTICAL_INSET, 0)
            }
        )

        adbSystemPathCheckbox = JBCheckBox(PluginBundle.message("adbUseSystemPath"))
        adbSystemPathCheckbox.isSelected = properties.useAdbFromPath
        adbSystemPathCheckbox.addItemListener {
            updateAdbLocationSettingsState()
        }
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

        adbLocationTitle = JBLabel(PluginBundle.message("adbPathTitle"))
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
        adbLocationField.textField.document.addDocumentListener(object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent) = verifyAdbLocation()
            override fun removeUpdate(e: DocumentEvent) = verifyAdbLocation()
            override fun changedUpdate(e: DocumentEvent) = verifyAdbLocation()
        })
        adbLocationField.addBrowseFolderListener(
            null,
            null,
            null,
            executableChooserDescriptor(),
            ExecutablePathTextComponentAccessor()
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
        defaultAdbLocationButton.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                adbLocationField.text = properties.defaultAdbLocation
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
    }

    private fun createScrcpySettings(panel: JPanel) {
        val helpButton = ContextHelpLabel.createWithLink(
            PluginBundle.message("scrcpyHelpTitle"),
            PluginBundle.message("scrcpyHelpDescription"),
            PluginBundle.message("scrcpyHelpLinkText")
        ) {
            BrowserUtil.browse(installScrcpyUrl)
        }
        val header = GridBagLayoutPanel().apply {
            add(JBLabel(PluginBundle.message("scrcpySettingsTitle")), GridBagConstraints())
            add(
                helpButton,
                GridBagConstraints().apply {
                    insets = Insets(0, 5, 0, 0)
                }
            )
            add(
                TitledSeparator(),
                GridBagConstraints().apply {
                    fill = GridBagConstraints.HORIZONTAL
                    weightx = 1.0
                }
            )
        }

        panel.add(
            header,
            GridBagConstraints().apply {
                gridx = 0
                gridy = 4
                gridwidth = 3
                fill = GridBagConstraints.HORIZONTAL
                weightx = 1.0
                insets = Insets(GROUP_VERTICAL_INSET, 0, GROUP_VERTICAL_INSET, 0)
            }
        )

        scrcpyEnabledCheckbox = JBCheckBox(PluginBundle.message("scrcpyEnabled"))
        scrcpyEnabledCheckbox.isSelected = properties.scrcpyEnabled
        scrcpyEnabledCheckbox.addItemListener {
            updateScrcpySettingsState()
        }
        panel.add(
            scrcpyEnabledCheckbox,
            GridBagConstraints().apply {
                gridx = 0
                gridy = 5
                gridwidth = 3
                anchor = GridBagConstraints.LINE_START
                insets = Insets(0, GROUP_LEFT_INSET, 4, 0)
            }
        )

        createScrcpyLocationSettings(panel)

        panel.add(
            createScrcpyOptionsPanel(),
            GridBagConstraints().apply {
                gridx = 0
                gridy = 9
                gridwidth = 3
                fill = GridBagConstraints.HORIZONTAL
                weightx = 1.0
                insets = Insets(GROUP_VERTICAL_INSET, GROUP_LEFT_INSET, 0, 0)
            }
        )
    }

    private fun createScrcpyLocationSettings(panel: JPanel) {
        scrcpySystemPathCheckbox = JBCheckBox(PluginBundle.message("scrcpyUseSystemPath"))
        scrcpySystemPathCheckbox.isSelected = properties.useScrcpyFromPath
        scrcpySystemPathCheckbox.addItemListener {
            updateScrcpySettingsState()
        }
        panel.add(
            scrcpySystemPathCheckbox,
            GridBagConstraints().apply {
                gridx = 0
                gridy = 6
                gridwidth = 3
                anchor = GridBagConstraints.LINE_START
                insets = Insets(0, GROUP_LEFT_INSET, 4, 0)
            }
        )

        scrcpyLocationTitle = JBLabel(PluginBundle.message("scrcpyPathTitle"))
        panel.add(
            scrcpyLocationTitle,
            GridBagConstraints().apply {
                gridx = 0
                gridy = 7
                gridwidth = 1
                anchor = GridBagConstraints.LINE_START
                insets = Insets(0, GROUP_LEFT_INSET, 0, 8)
            }
        )

        scrcpyLocationField = TextFieldWithBrowseButton()
        scrcpyLocationField.text = properties.scrcpyLocation
        scrcpyLocationField.textField.document.addDocumentListener(object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent) = verifyScrcpyLocation()
            override fun removeUpdate(e: DocumentEvent) = verifyScrcpyLocation()
            override fun changedUpdate(e: DocumentEvent) = verifyScrcpyLocation()
        })
        scrcpyLocationField.addBrowseFolderListener(
            null,
            null,
            null,
            executableChooserDescriptor(),
            ExecutablePathTextComponentAccessor()
        )
        panel.add(
            scrcpyLocationField,
            GridBagConstraints().apply {
                gridx = 1
                gridy = 7
                gridwidth = 2
                fill = GridBagConstraints.HORIZONTAL
                weightx = 1.0
            }
        )

        scrcpyStatusLabel = JBLabel()
        panel.add(
            scrcpyStatusLabel,
            GridBagConstraints().apply {
                gridx = 1
                gridy = 8
                gridwidth = 1
                fill = GridBagConstraints.HORIZONTAL
                weightx = 1.0
                insets = Insets(4, 0, 0, 0)
            }
        )
    }

    private fun createScrcpyOptionsPanel(): JComponent {
        val panel = GridBagLayoutPanel()

        val title = JBLabel(PluginBundle.message("scrcpyFlagsTitle"))
        panel.add(
            title,
            GridBagConstraints().apply {
                gridx = 0
                gridy = 0
                gridwidth = 2
                anchor = GridBagConstraints.LINE_START
            }
        )

        val textArea = JBTextArea(3, 1)
        textArea.lineWrap = true
        textArea.margin = Insets(8, 8, 8, 8)
        panel.add(
            textArea,
            GridBagConstraints().apply {
                gridx = 0
                gridy = 1
                gridwidth = 2
                fill = GridBagConstraints.HORIZONTAL
                weightx = 1.0
                weighty = 1.0
                insets = Insets(4, 0, 0, 0)
            }
        )

        val subtitle = JBLabel(PluginBundle.message("scrcpyFlagsSubtitle"))
        subtitle.componentStyle = UIUtil.ComponentStyle.SMALL
        subtitle.fontColor = UIUtil.FontColor.BRIGHTER
        panel.add(
            subtitle,
            GridBagConstraints().apply {
                gridx = 0
                gridy = 2
                anchor = GridBagConstraints.LINE_START
                insets = Insets(4, 0, 0, 0)
            }
        )

        val docLink = HyperlinkLabel(PluginBundle.message("scrcpyDocLabel"))
        docLink.setHyperlinkTarget("https://github.com/Genymobile/scrcpy#features")
        panel.add(
            docLink,
            GridBagConstraints().apply {
                gridx = 1
                gridy = 2
                anchor = GridBagConstraints.LINE_END
                insets = Insets(4, 0, 0, 0)
            }
        )

        return panel
    }

    override fun isModified(): Boolean {
        if (adbSystemPathCheckbox.isSelected != properties.useAdbFromPath) return true
        if (adbLocationField.text != properties.adbLocation) return true

        if (SCRCPY_FEATURE_ENABLED) {
            if (scrcpyEnabledCheckbox.isSelected != properties.scrcpyEnabled) return true
            if (scrcpySystemPathCheckbox.isSelected != properties.useScrcpyFromPath) return true
            if (scrcpyLocationField.text != properties.scrcpyLocation) return true
        }

        return false
    }

    override fun apply() {
        properties.adbLocation = adbLocationField.text
        properties.useAdbFromPath = adbSystemPathCheckbox.isSelected

        if (SCRCPY_FEATURE_ENABLED) {
            properties.scrcpyEnabled = scrcpyEnabledCheckbox.isSelected
            properties.scrcpyLocation = scrcpyLocationField.text
            properties.useScrcpyFromPath = scrcpySystemPathCheckbox.isSelected
        }
    }

    override fun reset() {
        adbSystemPathCheckbox.isSelected = properties.useAdbFromPath
        adbLocationField.text = properties.adbLocation

        if (SCRCPY_FEATURE_ENABLED) {
            scrcpyEnabledCheckbox.isSelected = properties.scrcpyEnabled
            scrcpySystemPathCheckbox.isSelected = properties.useScrcpyFromPath
            scrcpyLocationField.text = properties.scrcpyLocation
        }
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

        defaultAdbLocationButton.isVisible = adbStatusLabel.isVisible
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

    private fun updateAdbLocationSettingsState() {
        val enabled = !adbSystemPathCheckbox.isSelected
        adbLocationTitle.isEnabled = enabled
        adbLocationField.isEnabled = enabled
        adbStatusLabel.isVisible = enabled
        defaultAdbLocationButton.isVisible = enabled && adbStatusLabel.icon == ERROR_ICON
    }

    private fun updateScrcpySettingsState() {
        val scrcpyEnabled = scrcpyEnabledCheckbox.isSelected
        scrcpySystemPathCheckbox.isEnabled = scrcpyEnabled

        val locationEnabled = scrcpyEnabled && !scrcpySystemPathCheckbox.isSelected
        scrcpyLocationTitle.isEnabled = locationEnabled
        scrcpyLocationField.isEnabled = locationEnabled
        scrcpyStatusLabel.isVisible = locationEnabled
    }

    private class ExecutablePathTextComponentAccessor(
        val onTextChanged: (() -> Unit)? = null
    ) : TextComponentAccessor<JTextField> {

        override fun getText(component: JTextField): String = component.text

        override fun setText(component: JTextField, text: String) {
            val file = File(text)
            val dirName = if (file.isFile) file.parent.orEmpty() else text
            component.text = dirName

            onTextChanged?.invoke()
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

        private val installScrcpyUrl: String by lazy {
            when {
                SystemInfo.isLinux -> "https://github.com/Genymobile/scrcpy#linux"
                SystemInfo.isWindows -> "https://github.com/Genymobile/scrcpy#windows"
                SystemInfo.isMac -> "https://github.com/Genymobile/scrcpy#macos"
                else -> "https://github.com/Genymobile/scrcpy"
            }
        }
    }
}
