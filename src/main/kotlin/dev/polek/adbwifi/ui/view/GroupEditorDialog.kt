package dev.polek.adbwifi.ui.view

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import com.intellij.util.ui.JBUI
import dev.polek.adbwifi.PluginBundle
import dev.polek.adbwifi.model.CommandIcon
import dev.polek.adbwifi.model.CommandIconsProvider
import dev.polek.adbwifi.model.CommandListItem.AdbCommandGroup
import dev.polek.adbwifi.model.IconColor
import java.awt.BorderLayout
import java.awt.Cursor
import java.awt.Dimension
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.border.CompoundBorder

class GroupEditorDialog(
    existingGroup: AdbCommandGroup? = null
) : DialogWrapper(true) {

    private val nameField = JBTextField(30)
    private var selectedIcon: CommandIcon? = null
    private var selectedColor: IconColor = IconColor.PRIMARY
    private val iconButton = createIconButton()

    val groupName: String get() = nameField.text.trim()
    val iconId: String get() = selectedIcon?.id ?: ""
    val iconColor: String get() = selectedColor.colorName

    init {
        title = if (existingGroup != null) {
            PluginBundle.message("adbCommandGroupEditorTitleEdit")
        } else {
            PluginBundle.message("adbCommandGroupEditorTitleAdd")
        }
        init()

        existingGroup?.let { group ->
            nameField.text = group.name
            if (group.iconId.isNotEmpty()) {
                selectedIcon = CommandIconsProvider.getIconById(group.iconId)
                selectedColor = IconColor.fromString(group.iconColor)
                updateIconButton()
            }
        }
    }

    override fun createCenterPanel(): JComponent {
        return FormBuilder.createFormBuilder()
            .addLabeledComponent(PluginBundle.message("adbCommandGroupEditorNameLabel"), nameField)
            .addLabeledComponent(PluginBundle.message("adbCommandEditorIconLabel"), iconButton)
            .panel
    }

    override fun getPreferredFocusedComponent(): JComponent = nameField

    override fun doValidate(): ValidationInfo? {
        if (groupName.isBlank()) {
            return ValidationInfo(
                PluginBundle.message("adbCommandGroupEditorNameRequired"),
                nameField
            )
        }
        return null
    }

    private fun createIconButton(): JPanel {
        val panel = JPanel(BorderLayout(JBUI.scale(8), 0))
        panel.isOpaque = true
        panel.background = JBColor.background()
        panel.border = CompoundBorder(
            JBUI.Borders.customLine(JBColor.border()),
            JBUI.Borders.empty(4, 8)
        )
        panel.cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        panel.preferredSize = Dimension(0, JBUI.scale(28))

        panel.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                openIconPicker()
            }

            override fun mouseEntered(e: MouseEvent) {
                panel.background = JBUI.CurrentTheme.List.Hover.background(true)
            }

            override fun mouseExited(e: MouseEvent) {
                panel.background = JBColor.background()
            }
        })

        addIconContent(panel)
        return panel
    }

    private fun updateIconButton() {
        iconButton.removeAll()
        addIconContent(iconButton)
        iconButton.revalidate()
        iconButton.repaint()
    }

    private fun addIconContent(panel: JPanel) {
        val icon = selectedIcon
        if (icon != null) {
            val coloredIcon = CommandIconsProvider.getIcon(icon.id, selectedColor)
            if (coloredIcon != null) {
                panel.add(JBLabel(coloredIcon), BorderLayout.WEST)
            }
            panel.add(JBLabel(icon.displayName), BorderLayout.CENTER)

            val clearButton = object : JBLabel(com.intellij.icons.AllIcons.Actions.Close) {
                private var hovered = false
                private var pressed = false

                init {
                    toolTipText = PluginBundle.message("iconPickerClearIcon")
                    cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                    isOpaque = false
                    border = JBUI.Borders.empty(2)
                    addMouseListener(object : MouseAdapter() {
                        override fun mouseClicked(e: MouseEvent) {
                            e.consume()
                            selectedIcon = null
                            updateIconButton()
                        }
                        override fun mouseEntered(e: MouseEvent) { hovered = true; repaint() }
                        override fun mouseExited(e: MouseEvent) { hovered = false; pressed = false; repaint() }
                        override fun mousePressed(e: MouseEvent) { pressed = true; repaint() }
                        override fun mouseReleased(e: MouseEvent) { pressed = false; repaint() }
                    })
                }

                override fun paintComponent(g: java.awt.Graphics) {
                    if (hovered || pressed) {
                        val g2 = g as java.awt.Graphics2D
                        g2.setRenderingHint(
                            java.awt.RenderingHints.KEY_ANTIALIASING,
                            java.awt.RenderingHints.VALUE_ANTIALIAS_ON
                        )
                        g2.color = if (pressed) {
                            JBUI.CurrentTheme.ActionButton.pressedBackground()
                        } else {
                            JBUI.CurrentTheme.ActionButton.hoverBackground()
                        }
                        g2.fillRoundRect(0, 0, width, height, JBUI.scale(6), JBUI.scale(6))
                    }
                    super.paintComponent(g)
                }
            }
            panel.add(clearButton, BorderLayout.EAST)
        } else {
            panel.add(JBLabel(PluginBundle.message("iconPickerNoIcon")), BorderLayout.CENTER)
        }
    }

    private fun openIconPicker() {
        val dialog = IconPickerDialog(selectedIcon?.id, selectedColor)
        if (dialog.showAndGet()) {
            selectedIcon = dialog.selectedIcon
            selectedColor = dialog.selectedColor
            updateIconButton()
        }
    }
}
