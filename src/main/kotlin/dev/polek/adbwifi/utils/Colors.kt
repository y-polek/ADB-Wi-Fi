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
    val SEPARATOR: Color get() = JBColor.border()

    // Icon button colors - using platform background
    val ICON_BUTTON_BG: Color get() = JBColor.background()

    // Text colors - using platform colors
    val PRIMARY_TEXT: Color get() = JBColor.foreground()
    val SECONDARY_TEXT: Color get() = JBUI.CurrentTheme.ContextHelp.FOREGROUND

    val GREEN_BUTTON_BG = JBColor(0x5C9D69, 0x5C9D69)
    val GREEN_BUTTON_TEXT: Color = JBColor.WHITE

    val RED_BUTTON_BG = JBColor(0xFEF2F2, 0x1F1313)
    val RED_BUTTON_BORDER = JBColor(0xFECACA, 0x4D2828)
    val RED_BUTTON_TEXT = JBColor(0xDC2626, 0xC75450)
}
