package dev.polek.adbwifi.ui.view

import com.intellij.ide.BrowserUtil
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import dev.polek.adbwifi.PluginBundle
import dev.polek.adbwifi.utils.Colors
import dev.polek.adbwifi.utils.Icons
import dev.polek.adbwifi.utils.makeBold
import java.awt.Cursor
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.RenderingHints
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JButton
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
        contentPanel.add(Box.createVerticalStrut(8))

        // Description
        val descriptionLabel = JBLabel(PluginBundle.message("emptyStateDescription"))
        descriptionLabel.foreground = Colors.SECONDARY_TEXT
        descriptionLabel.componentStyle = UIUtil.ComponentStyle.REGULAR
        descriptionLabel.alignmentX = CENTER_ALIGNMENT
        contentPanel.add(descriptionLabel)
        contentPanel.add(Box.createVerticalStrut(40))

        // Buttons container
        val buttonsPanel = JPanel()
        buttonsPanel.layout = BoxLayout(buttonsPanel, BoxLayout.Y_AXIS)
        buttonsPanel.isOpaque = false
        buttonsPanel.alignmentX = CENTER_ALIGNMENT

        // "Enter IP & Port" button (primary green button)
        val enterIpButton = createPrimaryButton(PluginBundle.message("enterIpPortButton"))
        enterIpButton.addActionListener { onEnterIpPort() }
        enterIpButton.alignmentX = CENTER_ALIGNMENT
        buttonsPanel.add(enterIpButton)

        contentPanel.add(buttonsPanel)
        contentPanel.add(Box.createVerticalStrut(32))

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

        contentPanel.add(troubleshootingPanel)

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

    private fun createPrimaryButton(text: String): JButton {
        return object : JButton(text) {
            private val buttonIcon = Icons.PLUS

            init {
                preferredSize = Dimension(BUTTON_WIDTH, BUTTON_HEIGHT)
                maximumSize = Dimension(BUTTON_WIDTH, BUTTON_HEIGHT)
                isOpaque = false
                isContentAreaFilled = false
                isFocusPainted = false
                isBorderPainted = false
                background = Colors.GREEN_BUTTON_BG
                foreground = Colors.GREEN_BUTTON_TEXT
                font = font.deriveFont(java.awt.Font.BOLD)
            }

            override fun paintComponent(g: Graphics) {
                val g2 = g.create() as Graphics2D
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)

                // Draw background
                g2.color = if (model.isPressed) Colors.GREEN_BUTTON_BG.darker() else background
                g2.fillRoundRect(0, 0, width, height, BUTTON_CORNER_RADIUS * 2, BUTTON_CORNER_RADIUS * 2)

                // Calculate content dimensions
                val fm = g2.fontMetrics
                val textWidth = fm.stringWidth(text)
                val iconWidth = buttonIcon.iconWidth
                val iconTextGap = 8
                val totalWidth = iconWidth + iconTextGap + textWidth
                val startX = (width - totalWidth) / 2

                // Draw icon
                val iconY = (height - buttonIcon.iconHeight) / 2
                buttonIcon.paintIcon(this, g2, startX, iconY)

                // Draw text
                g2.color = foreground
                g2.font = font
                val textX = startX + iconWidth + iconTextGap
                val textY = (height - fm.height) / 2 + fm.ascent
                g2.drawString(text, textX, textY)

                g2.dispose()
            }
        }
    }

    private companion object {
        private const val CONTENT_PADDING = 32
        private const val TITLE_FONT_SIZE = 20f
        private const val BUTTON_WIDTH = 280
        private const val BUTTON_HEIGHT = 44
        private const val BUTTON_CORNER_RADIUS = 8
    }
}
