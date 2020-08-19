package dev.polek.adbwifi.utils

import java.awt.Toolkit
import java.awt.datatransfer.Clipboard
import java.awt.datatransfer.StringSelection

private val clipboard: Clipboard by lazy { Toolkit.getDefaultToolkit().systemClipboard }

fun copyToClipboard(text: String) {
    clipboard.setContents(StringSelection(text), null)
}
