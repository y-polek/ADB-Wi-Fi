package dev.polek.adbwifi.utils

import com.intellij.ui.JBColor

/**
 * Theme-aware color definitions for the plugin UI.
 * Colors are defined as JBColor(lightTheme, darkTheme).
 */
object Colors {
    // Background colors
    val PANEL_BACKGROUND = JBColor(0xF5F7FA, 0x1E1F22)
    val CARD_BACKGROUND = JBColor(0xFFFFFF, 0x2B2D30)

    // Border colors
    val CARD_BORDER = JBColor(0xE2E4E8, 0x393B40)
    val CONNECTED_CARD_BORDER = JBColor(0xE8F5E9, 0x354A3B)
    val SEPARATOR = JBColor(0xE2E4E8, 0x393B40)

    // Icon button colors
    val ICON_BUTTON_BG = JBColor(0xFFFFFF, 0x1E1F22)

    // Text colors
    val PRIMARY_TEXT = JBColor(0x2C2E33, 0xBCBEC4)
    val SECONDARY_TEXT = JBColor(0x6B7280, 0x7C7F88)

    // Button colors
    val GREEN_BUTTON_BG = JBColor(0x5C9D69, 0x5C9D69)
    val GREEN_BUTTON_TEXT = JBColor.WHITE

    val SECONDARY_BUTTON_BG = JBColor(0xFFFFFF, 0x2B2D30)
    val SECONDARY_BUTTON_BORDER = JBColor(0xE2E4E8, 0x393B40)
    val SECONDARY_BUTTON_TEXT = JBColor(0x2C2E33, 0xBCBEC4)

    val RED_BUTTON_BG = JBColor(0xFEF2F2, 0x1F1313)
    val RED_BUTTON_BORDER = JBColor(0xFECACA, 0x4D2828)
    val RED_BUTTON_TEXT = JBColor(0xDC2626, 0xC75450)
}
