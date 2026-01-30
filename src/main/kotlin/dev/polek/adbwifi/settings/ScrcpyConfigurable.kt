package dev.polek.adbwifi.settings

import com.intellij.ide.BrowserUtil
import com.intellij.openapi.components.service
import com.intellij.openapi.fileChooser.FileChooser
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
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import dev.polek.adbwifi.PluginBundle
import dev.polek.adbwifi.services.PropertiesService
import dev.polek.adbwifi.utils.GridBagLayoutPanel
import dev.polek.adbwifi.utils.Icons
import dev.polek.adbwifi.utils.isValidScrcpyLocation
import dev.polek.adbwifi.utils.onTextChanged
import dev.polek.adbwifi.utils.panel
import java.awt.GridBagConstraints
import javax.swing.JComponent
import javax.swing.JPanel

class ScrcpyConfigurable : Configurable {

    private val properties = service<PropertiesService>()

    private lateinit var scrcpyEnabledCheckbox: JBCheckBox
    private lateinit var scrcpySystemPathCheckbox: JBCheckBox
    private lateinit var scrcpyLocationTitle: JBLabel
    private lateinit var scrcpyLocationField: TextFieldWithBrowseButton
    private lateinit var scrcpyStatusLabel: JBLabel
    private lateinit var scrcpyCmdFlagsTitle: JBLabel
    private lateinit var scrcpyCmdFlagsTextArea: JBTextArea

    override fun getDisplayName(): String = PluginBundle.message("scrcpySettingsTitle")

    override fun createComponent(): JComponent {
        val panel = createScrcpySettingsPanel()
        verifyScrcpyLocation()
        updateScrcpySettingsState()
        return panel(top = panel)
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
            text = properties.scrcpyCmdFlags
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

    private fun executableChooserDescriptor() = when {
        SystemInfo.isMac -> FileChooserDescriptorFactory.createSingleFileOrFolderDescriptor()
        else -> FileChooserDescriptorFactory.createSingleFolderDescriptor()
    }

    private fun verifyScrcpyLocation() {
        val dir = scrcpyLocationField.text
        if (isValidScrcpyLocation(dir)) {
            scrcpyStatusLabel.apply {
                icon = Icons.OK
                text = PluginBundle.message("adbPathVerifiedMessage")
                foreground = JBColor.foreground()
            }
        } else {
            scrcpyStatusLabel.apply {
                icon = Icons.ERROR
                text = PluginBundle.message("scrcpyPathVerificationErrorMessage")
                foreground = JBColor.RED
            }
        }
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

    override fun isModified(): Boolean {
        if (scrcpyEnabledCheckbox.isSelected != properties.isScrcpyEnabled.value) return true
        if (scrcpySystemPathCheckbox.isSelected != properties.useScrcpyFromPath) return true
        if (scrcpyLocationField.text != properties.scrcpyLocation) return true
        if (scrcpyCmdFlagsTextArea.text.trim() != properties.scrcpyCmdFlags) return true
        return false
    }

    override fun apply() {
        properties.setScrcpyEnabled(scrcpyEnabledCheckbox.isSelected)
        properties.scrcpyLocation = scrcpyLocationField.text
        properties.useScrcpyFromPath = scrcpySystemPathCheckbox.isSelected
        properties.scrcpyCmdFlags = scrcpyCmdFlagsTextArea.text.trim()
    }

    override fun reset() {
        scrcpyEnabledCheckbox.isSelected = properties.isScrcpyEnabled.value
        scrcpySystemPathCheckbox.isSelected = properties.useScrcpyFromPath
        scrcpyLocationField.text = properties.scrcpyLocation
        scrcpyCmdFlagsTextArea.text = properties.scrcpyCmdFlags
        updateScrcpySettingsState()
    }

    private companion object {
        private const val GROUP_VERTICAL_INSET = 10
        private const val GROUP_LEFT_INSET = 20
        private const val COMPONENT_VERTICAL_INSET = 4
    }
}
