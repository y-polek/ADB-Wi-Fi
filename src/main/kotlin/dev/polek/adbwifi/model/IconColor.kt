package dev.polek.adbwifi.model

import com.intellij.ui.JBColor
import java.awt.Color

enum class IconColor(val colorName: String, val displayName: String, val color: JBColor) {
    PRIMARY("primary", "Default", JBColor(Color(0x6C, 0x70, 0x7E), Color(0xCE, 0xD0, 0xD6))),
    RED("red", "Red", JBColor(Color(0xDB, 0x58, 0x60), Color(0xC7, 0x54, 0x50))),
    GREEN("green", "Green", JBColor(Color(0x59, 0xA8, 0x69), Color(0x49, 0x9C, 0x54))),
    ORANGE("orange", "Orange", JBColor(Color(0xE8, 0xA3, 0x3D), Color(0xD9, 0xA3, 0x43)));

    companion object {
        fun fromString(value: String?): IconColor =
            entries.find { it.colorName == value } ?: PRIMARY
    }
}
