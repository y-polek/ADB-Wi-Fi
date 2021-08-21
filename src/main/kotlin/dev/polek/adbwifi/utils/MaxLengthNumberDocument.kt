package dev.polek.adbwifi.utils

import com.intellij.ui.NumberDocument
import java.awt.Toolkit
import javax.swing.text.AttributeSet

class MaxLengthNumberDocument(private val maxLength: Int) : NumberDocument() {
    override fun insertString(offs: Int, str: String, a: AttributeSet?) {
        if (length + str.length <= maxLength) {
            super.insertString(offs, str, a)
        } else {
            Toolkit.getDefaultToolkit().beep()
        }
    }
}
