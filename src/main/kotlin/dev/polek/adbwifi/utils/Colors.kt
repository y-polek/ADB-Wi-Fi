package dev.polek.adbwifi.utils

import com.intellij.ui.JBColor
import com.intellij.util.ui.JBUI
import java.awt.Color

/**
 * Theme-aware color definitions for the plugin UI.
 * Uses platform colors where possible for better theme integration.
 */
object Colors {
    // Background colors - using platform colors
    val PANEL_BACKGROUND: Color get() = JBUI.CurrentTheme.ToolWindow.background()
    val CARD_BACKGROUND: Color get() = JBUI.CurrentTheme.EditorTabs.background()

    // Border colors - using platform colors
    val CARD_BORDER: Color get() = JBColor.border()

    // Separator - more visible in dark theme
    val SEPARATOR = JBColor(0xE5E7EB, 0x4B4B4B)

    // Icon button colors
    val ICON_BUTTON_BG: Color get() = JBColor.background()

    // Hover color for secondary/icon buttons - distinct from panel background
    val BUTTON_HOVER_BG = JBColor(0xE8E8E8, 0x3C3C3C)

    // Disabled button background - distinct from panel background
    val DISABLED_BUTTON_BG = JBColor(0xE5E7EB, 0x4B4B4B)

    // Text colors - using platform colors
    val PRIMARY_TEXT: Color get() = JBColor.foreground()
    val SECONDARY_TEXT: Color get() = JBUI.CurrentTheme.ContextHelp.FOREGROUND

    // Green connect button - same in both themes
    val GREEN_BUTTON_BG = JBColor(0x5C9D69, 0x5C9D69)
    val GREEN_BUTTON_TEXT = JBColor(0xFFFFFF, 0xFFFFFF)

    val RED_BUTTON_BG = JBColor(0xFEF2F2, 0x1F1313)
    val RED_BUTTON_BORDER = JBColor(0xFECACA, 0x4D2828)
    val RED_BUTTON_TEXT = JBColor(0xDC2626, 0xC75450)
}
