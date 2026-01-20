package dev.polek.adbwifi.settings

import com.intellij.ide.BrowserUtil
import com.intellij.openapi.components.service
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.ui.ContextHelpLabel
import com.intellij.ui.HyperlinkLabel
import com.intellij.ui.JBColor
import com.intellij.ui.TitledSeparator
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextArea
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import dev.polek.adbwifi.PluginBundle
import dev.polek.adbwifi.services.AdbCommandsService
import dev.polek.adbwifi.services.PropertiesService
import dev.polek.adbwifi.utils.*
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*

class AdbWifiConfigurable : Configurable {

    private val properties = service<PropertiesService>()
    private val commandsService = service<AdbCommandsService>()

    private lateinit var adbPortField: JTextField
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
    private lateinit var scrcpyCmdFlagsTitle: JBLabel
    private lateinit var scrcpyCmdFlagsTextArea: JBTextArea

    private lateinit var confirmDeviceRemovalCheckbox: JBCheckBox

    private lateinit var commandsPanel: AdbCommandsSettingsPanel

    override fun getDisplayName(): String {
        return PluginBundle.message("settingsPageName")
    }

    override fun createComponent(): JComponent {
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.PAGE_AXIS)

        panel.add(createAdbSettingsPanel())
        panel.add(Box.createRigidArea(Dimension(0, GROUP_VERTICAL_INSET)))
        panel.add(createScrcpySettingsPanel())
        panel.add(Box.createRigidArea(Dimension(0, GROUP_VERTICAL_INSET)))
        panel.add(createAdbCommandsSettingsPanel())
        panel.add(Box.createRigidArea(Dimension(0, GROUP_VERTICAL_INSET)))
        panel.add(createGeneralSettingsPanel())

        verifyAdbLocation()
        updateAdbLocationSettingsState()

        verifyScrcpyLocation()
        updateScrcpySettingsState()

        return panel(top = panel)
    }

    private fun createAdbPortField() = JBTextField(7).apply {
        document = MaxLengthNumberDocument(5)
        makeMonospaced()
        text = properties.adbPort.toString()
    }

    private fun createAdbSystemPathCheckbox() = JBCheckBox(PluginBundle.message("adbUseSystemPath")).apply {
        isSelected = properties.useAdbFromPath
        addItemListener {
            updateAdbLocationSettingsState()
        }
    }

    private fun createAdbLocationField() = TextFieldWithBrowseButton().apply {
        text = properties.adbLocation
        textField.onTextChanged(::verifyAdbLocation)
        addActionListener {
            val currentPath = textField.text.takeIf { it.isNotBlank() }?.let {
                LocalFileSystem.getInstance().findFileByPath(it)
            }
            FileChooser.chooseFile(
                executableChooserDescriptor(),
                null,
                this,
                currentPath
            ) { selectedFile ->
                val path = if (selectedFile.isDirectory) {
                    selectedFile.path
                } else {
                    selectedFile.parent?.path.orEmpty()
                }
                textField.text = path
            }
        }
    }

    private fun createDefaultAdbLocationButton() =
        HyperlinkLabel(PluginBundle.message("defaultAdbLocationButton")).apply {
            addMouseListener(object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent) {
                    adbLocationField.text = properties.defaultAdbLocation
                }
            })
        }

    private fun createAdbSettingsPanel(): JPanel {
        val panel = GridBagLayoutPanel()

        val separator = TitledSeparator(PluginBundle.message("adbSettingsTitle"))
        panel.add(
            separator,
            GridBagConstraints().apply {
                gridx = 0
                gridy = 0
                gridwidth = 3
                fill = GridBagConstraints.HORIZONTAL
                weightx = 1.0
            }
        )

        @Suppress("DialogTitleCapitalization")
        val adbPortTitle = JBLabel(PluginBundle.message("adbPortTitle"))
        panel.add(
            adbPortTitle,
            GridBagConstraints().apply {
                gridx = 0
                gridy = 1
                gridwidth = 1
                anchor = GridBagConstraints.LINE_START
                insets = JBUI.insets(COMPONENT_VERTICAL_INSET, GROUP_LEFT_INSET, 0, 8)
            }
        )

        adbPortField = createAdbPortField()
        panel.add(
            adbPortField,
            GridBagConstraints().apply {
                gridx = 1
                gridy = 1
                gridwidth = 1
                fill = GridBagConstraints.HORIZONTAL
                weightx = 1.0
                insets = JBUI.insetsTop(COMPONENT_VERTICAL_INSET)
            }
        )

        adbSystemPathCheckbox = createAdbSystemPathCheckbox()
        panel.add(
            adbSystemPathCheckbox,
            GridBagConstraints().apply {
                gridx = 0
                gridy = 2
                gridwidth = 3
                anchor = GridBagConstraints.LINE_START
                insets = JBUI.insets(GROUP_VERTICAL_INSET, GROUP_LEFT_INSET, 0, 0)
            }
        )

        adbLocationTitle = JBLabel(PluginBundle.message("adbPathTitle"))
        panel.add(
            adbLocationTitle,
            GridBagConstraints().apply {
                gridx = 0
                gridy = 3
                gridwidth = 1
                anchor = GridBagConstraints.LINE_START
                insets = JBUI.insets(COMPONENT_VERTICAL_INSET, GROUP_LEFT_INSET, 0, 8)
            }
        )

        adbLocationField = createAdbLocationField()
        panel.add(
            adbLocationField,
            GridBagConstraints().apply {
                gridx = 1
                gridy = 3
                gridwidth = 2
                fill = GridBagConstraints.HORIZONTAL
                weightx = 1.0
                insets = JBUI.insetsTop(COMPONENT_VERTICAL_INSET)
            }
        )

        adbStatusLabel = JBLabel()
        panel.add(
            adbStatusLabel,
            GridBagConstraints().apply {
                gridx = 1
                gridy = 4
                gridwidth = 1
                fill = GridBagConstraints.HORIZONTAL
                weightx = 1.0
                insets = JBUI.insetsTop(COMPONENT_VERTICAL_INSET)
            }
        )

        defaultAdbLocationButton = createDefaultAdbLocationButton()
        panel.add(
            defaultAdbLocationButton,
            GridBagConstraints().apply {
                gridx = 2
                gridy = 4
                gridwidth = 1
                insets = JBUI.insetsTop(4)
            }
        )

        return panel
    }

    private fun createScrcpySettingsPanel(): JPanel {
        val panel = GridBagLayoutPanel()

        @Suppress("DialogTitleCapitalization")
        val helpButton = ContextHelpLabel.createWithLink(
            PluginBundle.message("scrcpyHelpTitle"),
            PluginBundle.message("scrcpyHelpDescription"),
            PluginBundle.message("scrcpyHelpLinkText")
        ) {
            BrowserUtil.browse("https://github.com/Genymobile/scrcpy#get-the-app")
        }
        val header = GridBagLayoutPanel().apply {
            withMinimumHeight(28)

            @Suppress("DialogTitleCapitalization")
            add(JBLabel(PluginBundle.message("scrcpySettingsTitle")), GridBagConstraints())
            add(
                helpButton,
                GridBagConstraints().apply {
                    insets = JBUI.insetsLeft(5)
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
                gridy = 0
                gridwidth = 3
                fill = GridBagConstraints.HORIZONTAL
                weightx = 1.0
            }
        )

        scrcpyEnabledCheckbox = JBCheckBox(PluginBundle.message("scrcpyEnabled"))
        scrcpyEnabledCheckbox.isSelected = properties.isScrcpyEnabled.value
        scrcpyEnabledCheckbox.addItemListener {
            updateScrcpySettingsState()
        }
        panel.add(
            scrcpyEnabledCheckbox,
            GridBagConstraints().apply {
                gridx = 0
                gridy = 1
                gridwidth = 3
                anchor = GridBagConstraints.LINE_START
                insets = JBUI.insets(GROUP_VERTICAL_INSET, GROUP_LEFT_INSET, 0, 0)
            }
        )

        panel.add(
            createScrcpyLocationSettingsPanel(),
            GridBagConstraints().apply {
                gridx = 0
                gridy = 2
                gridwidth = 3
                fill = GridBagConstraints.HORIZONTAL
                weightx = 1.0
                insets = JBUI.insets(GROUP_VERTICAL_INSET, GROUP_LEFT_INSET, 0, 0)
            }
        )

        panel.add(
            createScrcpyOptionsPanel(),
            GridBagConstraints().apply {
                gridx = 0
                gridy = 3
                gridwidth = 3
                fill = GridBagConstraints.HORIZONTAL
                weightx = 1.0
                insets = JBUI.insets(GROUP_VERTICAL_INSET, GROUP_LEFT_INSET, 0, 0)
            }
        )

        return panel
    }

    private fun createScrcpyLocationSettingsPanel(): JPanel {
        val panel = GridBagLayoutPanel()

        scrcpySystemPathCheckbox = JBCheckBox(PluginBundle.message("scrcpyUseSystemPath"))
        scrcpySystemPathCheckbox.isSelected = properties.useScrcpyFromPath
        scrcpySystemPathCheckbox.addItemListener {
            updateScrcpySettingsState()
        }
        panel.add(
            scrcpySystemPathCheckbox,
            GridBagConstraints().apply {
                gridx = 0
                gridy = 0
                gridwidth = 3
                anchor = GridBagConstraints.LINE_START
            }
        )

        scrcpyLocationTitle = JBLabel(PluginBundle.message("scrcpyPathTitle"))
        panel.add(
            scrcpyLocationTitle,
            GridBagConstraints().apply {
                gridx = 0
                gridy = 1
                gridwidth = 1
                anchor = GridBagConstraints.LINE_START
                insets = JBUI.insets(COMPONENT_VERTICAL_INSET, 0, 0, 8)
            }
        )

        scrcpyLocationField = TextFieldWithBrowseButton()
        scrcpyLocationField.text = properties.scrcpyLocation
        scrcpyLocationField.textField.onTextChanged(::verifyScrcpyLocation)
        scrcpyLocationField.addActionListener {
            val currentPath = scrcpyLocationField.text.takeIf { it.isNotBlank() }?.let {
                LocalFileSystem.getInstance().findFileByPath(it)
            }
            FileChooser.chooseFile(
                executableChooserDescriptor(),
                null,
                scrcpyLocationField,
                currentPath
            ) { selectedFile ->
                val path = if (selectedFile.isDirectory) {
                    selectedFile.path
                } else {
                    selectedFile.parent?.path.orEmpty()
                }
                scrcpyLocationField.text = path
            }
        }
        panel.add(
            scrcpyLocationField,
            GridBagConstraints().apply {
                gridx = 1
                gridy = 1
                gridwidth = 2
                fill = GridBagConstraints.HORIZONTAL
                weightx = 1.0
                insets = JBUI.insetsTop(COMPONENT_VERTICAL_INSET)
            }
        )

        scrcpyStatusLabel = JBLabel()
        panel.add(
            scrcpyStatusLabel,
            GridBagConstraints().apply {
                gridx = 1
                gridy = 2
                gridwidth = 1
                fill = GridBagConstraints.HORIZONTAL
                weightx = 1.0
                insets = JBUI.insetsTop(COMPONENT_VERTICAL_INSET)
            }
        )

        return panel
    }

    private fun createScrcpyOptionsPanel(): JPanel {
        val panel = GridBagLayoutPanel()

        scrcpyCmdFlagsTitle = JBLabel(PluginBundle.message("scrcpyFlagsTitle"))
        panel.add(
            scrcpyCmdFlagsTitle,
            GridBagConstraints().apply {
                gridx = 0
                gridy = 0
                gridwidth = 2
                anchor = GridBagConstraints.LINE_START
            }
        )

        scrcpyCmdFlagsTextArea = JBTextArea(3, 1).apply {
            lineWrap = true
            margin = JBUI.insets(8)
        }
        val scrcpyCmdFlagsScrollPane = JBScrollPane(scrcpyCmdFlagsTextArea)
        panel.add(
            scrcpyCmdFlagsScrollPane,
            GridBagConstraints().apply {
                gridx = 0
                gridy = 1
                gridwidth = 2
                fill = GridBagConstraints.HORIZONTAL
                weightx = 1.0
                weighty = 1.0
                insets = JBUI.insetsTop(4)
            }
        )

        @Suppress("DialogTitleCapitalization")
        val subtitle = JBLabel(PluginBundle.message("scrcpyFlagsSubtitle"))
        subtitle.componentStyle = UIUtil.ComponentStyle.SMALL
        subtitle.fontColor = UIUtil.FontColor.BRIGHTER
        subtitle.setCopyable(true)
        panel.add(
            subtitle,
            GridBagConstraints().apply {
                gridx = 0
                gridy = 2
                anchor = GridBagConstraints.LINE_START
                insets = JBUI.insetsTop(4)
            }
        )

        val docLink = HyperlinkLabel(PluginBundle.message("scrcpyDocLabel"))
        docLink.setHyperlinkTarget("https://github.com/Genymobile/scrcpy#user-documentation")
        panel.add(
            docLink,
            GridBagConstraints().apply {
                gridx = 1
                gridy = 2
                anchor = GridBagConstraints.LINE_END
                insets = JBUI.insetsTop(4)
            }
        )

        return panel
    }

    private fun createAdbCommandsSettingsPanel(): JPanel {
        val panel = GridBagLayoutPanel()

        val separator = TitledSeparator(PluginBundle.message("adbCommandsSettingsTitle"))
        panel.add(
            separator,
            GridBagConstraints().apply {
                gridx = 0
                gridy = 0
                fill = GridBagConstraints.HORIZONTAL
                weightx = 1.0
            }
        )

        commandsPanel = AdbCommandsSettingsPanel()
        commandsPanel.loadFromService(commandsService)
        panel.add(
            commandsPanel,
            GridBagConstraints().apply {
                gridx = 0
                gridy = 1
                fill = GridBagConstraints.BOTH
                weightx = 1.0
                weighty = 1.0
                insets = JBUI.insets(GROUP_VERTICAL_INSET, GROUP_LEFT_INSET, 0, 0)
            }
        )

        return panel
    }

    private fun createGeneralSettingsPanel(): JPanel {
        val panel = GridBagLayoutPanel()

        val separator = TitledSeparator(PluginBundle.message("generalSettingsTitle"))
        panel.add(
            separator,
            GridBagConstraints().apply {
                gridx = 0
                gridy = 0
                fill = GridBagConstraints.HORIZONTAL
                weightx = 1.0
            }
        )

        confirmDeviceRemovalCheckbox = JBCheckBox(PluginBundle.message("confirmDeviceRemoval"))
        confirmDeviceRemovalCheckbox.isSelected = properties.confirmDeviceRemoval
        panel.add(
            confirmDeviceRemovalCheckbox,
            GridBagConstraints().apply {
                gridx = 0
                gridy = 1
                anchor = GridBagConstraints.LINE_START
                insets = JBUI.insets(GROUP_VERTICAL_INSET, GROUP_LEFT_INSET, 0, 0)
            }
        )

        return panel
    }

    override fun isModified(): Boolean {
        if (adbSystemPathCheckbox.isSelected != properties.useAdbFromPath) return true
        if (adbLocationField.text != properties.adbLocation) return true
        if ((adbPortField.text.toIntOrNull() ?: ADB_DEFAULT_PORT) != properties.adbPort) return true

        if (scrcpyEnabledCheckbox.isSelected != properties.isScrcpyEnabled.value) return true
        if (scrcpySystemPathCheckbox.isSelected != properties.useScrcpyFromPath) return true
        if (scrcpyLocationField.text != properties.scrcpyLocation) return true
        if (scrcpyCmdFlagsTextArea.text.trim() != properties.scrcpyCmdFlags) return true

        if (confirmDeviceRemovalCheckbox.isSelected != properties.confirmDeviceRemoval) return true

        if (commandsPanel.isModified(commandsService)) return true

        return false
    }

    override fun apply() {
        properties.adbLocation = adbLocationField.text
        properties.useAdbFromPath = adbSystemPathCheckbox.isSelected
        properties.adbPort = adbPortField.text.toIntOrNull() ?: ADB_DEFAULT_PORT
        adbPortField.text = properties.adbPort.toString()

        properties.setScrcpyEnabled(scrcpyEnabledCheckbox.isSelected)
        properties.scrcpyLocation = scrcpyLocationField.text
        properties.useScrcpyFromPath = scrcpySystemPathCheckbox.isSelected
        properties.scrcpyCmdFlags = scrcpyCmdFlagsTextArea.text.trim()

        properties.confirmDeviceRemoval = confirmDeviceRemovalCheckbox.isSelected

        commandsPanel.applyToService(commandsService)
    }

    override fun reset() {
        adbSystemPathCheckbox.isSelected = properties.useAdbFromPath
        adbLocationField.text = properties.adbLocation
        adbPortField.text = properties.adbPort.toString()

        scrcpyEnabledCheckbox.isSelected = properties.isScrcpyEnabled.value
        scrcpySystemPathCheckbox.isSelected = properties.useScrcpyFromPath
        scrcpyLocationField.text = properties.scrcpyLocation
        scrcpyCmdFlagsTextArea.text = properties.scrcpyCmdFlags

        confirmDeviceRemovalCheckbox.isSelected = properties.confirmDeviceRemoval

        commandsPanel.loadFromService(commandsService)
    }

    private fun executableChooserDescriptor(): FileChooserDescriptor = when {
        SystemInfo.isMac -> FileChooserDescriptorFactory.createSingleFileOrFolderDescriptor()
        else -> FileChooserDescriptorFactory.createSingleFolderDescriptor()
    }

    private fun showAdbVerifiedMessage() {
        adbStatusLabel.apply {
            icon = Icons.OK
            text = PluginBundle.message("adbPathVerifiedMessage")
            foreground = JBColor.foreground()
        }

        defaultAdbLocationButton.isVisible = false
    }

    private fun showScrcpyVerifiedMessage() {
        scrcpyStatusLabel.apply {
            icon = Icons.OK
            text = PluginBundle.message("adbPathVerifiedMessage")
            foreground = JBColor.foreground()
        }
    }

    private fun showAdbVerificationErrorMessage() {
        adbStatusLabel.apply {
            icon = Icons.ERROR
            text = PluginBundle.message("adbPathVerificationErrorMessage")
            foreground = JBColor.RED
        }

        defaultAdbLocationButton.isVisible = adbStatusLabel.isVisible
    }

    private fun showScrcpyVerificationErrorMessage() {
        scrcpyStatusLabel.apply {
            icon = Icons.ERROR
            text = PluginBundle.message("scrcpyPathVerificationErrorMessage")
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
        defaultAdbLocationButton.isVisible = enabled && adbStatusLabel.icon == Icons.ERROR
    }

    private fun updateScrcpySettingsState() {
        val scrcpyEnabled = scrcpyEnabledCheckbox.isSelected
        scrcpySystemPathCheckbox.isEnabled = scrcpyEnabled
        scrcpyCmdFlagsTitle.isEnabled = scrcpyEnabled
        scrcpyCmdFlagsTextArea.isEnabled = scrcpyEnabled

        val locationEnabled = scrcpyEnabled && !scrcpySystemPathCheckbox.isSelected
        scrcpyLocationTitle.isEnabled = locationEnabled
        scrcpyLocationField.isEnabled = locationEnabled
        scrcpyStatusLabel.isVisible = locationEnabled
    }

    private companion object {
        private const val GROUP_VERTICAL_INSET = 10
        private const val GROUP_LEFT_INSET = 20
        private const val COMPONENT_VERTICAL_INSET = 4
    }
}
