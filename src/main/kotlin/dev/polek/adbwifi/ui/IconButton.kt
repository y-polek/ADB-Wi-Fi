package dev.polek.adbwifi.ui

import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import dev.polek.adbwifi.utils.AbstractMouseListener
import java.awt.event.MouseEvent
import javax.swing.Icon

class IconButton(icon: Icon, tooltip: String? = null) : JBLabel() {

    var onClickedListener: ((event: MouseEvent) -> Unit)? = null

    init {
        this.icon = icon
        background = HOVER_COLOR
        toolTipText = tooltip

        addMouseListener(object : AbstractMouseListener() {
            override fun mouseClicked(e: MouseEvent) {
                onClickedListener?.invoke(e)
            }

            override fun mouseEntered(e: MouseEvent) {
                isOpaque = true
            }

            override fun mouseExited(e: MouseEvent) {
                isOpaque = false
            }

            override fun mousePressed(e: MouseEvent) {
                background = PRESSED_COLOR
            }

            override fun mouseReleased(e: MouseEvent) {
                background = HOVER_COLOR
            }
        })
    }

    private companion object {
        private val HOVER_COLOR = JBColor(0xdfdfdf, 0x4b5052)
        private val PRESSED_COLOR = JBColor(0xcfcfcf, 0x5b6164)
    }
}
