package dev.polek.adbwifi.ui.view

import com.intellij.ide.BrowserUtil
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import dev.polek.adbwifi.PluginBundle
import dev.polek.adbwifi.ui.view.buttons.StyledButton
import dev.polek.adbwifi.utils.Colors
import dev.polek.adbwifi.utils.Icons
import dev.polek.adbwifi.utils.makeBold
import java.awt.Cursor
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JPanel

/**
 * Panel displayed when no devices are connected.
 * Shows a message and buttons to connect a device.
 */
class EmptyStatePanel(
    private val onEnterIpPort: () -> Unit
) : JBPanel<EmptyStatePanel>(GridBagLayout()) {

    init {
        background = Colors.PANEL_BACKGROUND

        val contentPanel = JPanel()
        contentPanel.layout = BoxLayout(contentPanel, BoxLayout.Y_AXIS)
        contentPanel.isOpaque = false
        contentPanel.border = JBUI.Borders.empty(CONTENT_PADDING)

        // Title
        val titleLabel = JBLabel(PluginBundle.message("emptyStateTitle"))
        titleLabel.foreground = Colors.PRIMARY_TEXT
        titleLabel.makeBold()
        titleLabel.font = titleLabel.font.deriveFont(TITLE_FONT_SIZE)
        titleLabel.alignmentX = CENTER_ALIGNMENT
        contentPanel.add(titleLabel)
        contentPanel.add(Box.createVerticalStrut(10))

        // Description
        val descriptionText = PluginBundle.message("emptyStateDescription")
        val descriptionLabel = JBLabel("<html><center>$descriptionText</center></html>")
        descriptionLabel.foreground = Colors.SECONDARY_TEXT
        descriptionLabel.componentStyle = UIUtil.ComponentStyle.REGULAR
        descriptionLabel.alignmentX = CENTER_ALIGNMENT
        contentPanel.add(descriptionLabel)
        contentPanel.add(Box.createVerticalStrut(20))

        // "Enter IP & Port" button (primary green button)
        val enterIpButton = StyledButton(
            text = PluginBundle.message("enterIpPortButton"),
            style = StyledButton.Style.PRIMARY,
            icon = Icons.PLUS_WHITE
        ).apply {
            preferredSize = Dimension(BUTTON_WIDTH, BUTTON_HEIGHT)
            minimumSize = Dimension(BUTTON_WIDTH, BUTTON_HEIGHT)
            maximumSize = Dimension(BUTTON_WIDTH, BUTTON_HEIGHT)
            alignmentX = CENTER_ALIGNMENT
            addActionListener { onEnterIpPort() }
        }
        contentPanel.add(enterIpButton)
        contentPanel.add(Box.createVerticalStrut(20))

        // Troubleshooting guide link
        val troubleshootingPanel = JPanel(FlowLayout(FlowLayout.CENTER, 6, 0))
        troubleshootingPanel.isOpaque = false
        troubleshootingPanel.alignmentX = CENTER_ALIGNMENT

        val helpIconLabel = JBLabel(Icons.HELP)
        helpIconLabel.foreground = Colors.SECONDARY_TEXT
        troubleshootingPanel.add(helpIconLabel)

        val troubleshootingLabel = JBLabel(PluginBundle.message("troubleshootingGuideLink"))
        troubleshootingLabel.foreground = Colors.SECONDARY_TEXT
        troubleshootingLabel.componentStyle = UIUtil.ComponentStyle.SMALL
        troubleshootingLabel.cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        troubleshootingLabel.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                val url = PluginBundle.message("troubleshootingGuideUrl")
                BrowserUtil.browse(url)
            }

            override fun mouseEntered(e: MouseEvent) {
                troubleshootingLabel.text = "<html><u>${PluginBundle.message("troubleshootingGuideLink")}</u></html>"
            }

            override fun mouseExited(e: MouseEvent) {
                troubleshootingLabel.text = PluginBundle.message("troubleshootingGuideLink")
            }
        })
        troubleshootingPanel.add(troubleshootingLabel)
        troubleshootingPanel.maximumSize = Dimension(Int.MAX_VALUE, troubleshootingPanel.preferredSize.height)

        contentPanel.add(troubleshootingPanel)
        contentPanel.add(Box.createVerticalGlue())

        add(
            contentPanel,
            GridBagConstraints().apply {
                gridx = 0
                gridy = 0
                weightx = 1.0
                weighty = 1.0
                anchor = GridBagConstraints.CENTER
            }
        )
    }

    private companion object {
        private const val CONTENT_PADDING = 10
        private const val TITLE_FONT_SIZE = 20f
        private const val BUTTON_WIDTH = 200
        private const val BUTTON_HEIGHT = 32
    }
}
