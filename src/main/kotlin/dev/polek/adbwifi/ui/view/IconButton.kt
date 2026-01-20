package dev.polek.adbwifi.ui.view

import com.intellij.ui.AnimatedIcon
import com.intellij.ui.InplaceButton
import com.intellij.util.ui.JBUI
import dev.polek.adbwifi.utils.appCoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.awt.Graphics
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.Icon

class IconButton(
    private val originalIcon: Icon,
    tooltip: String? = null
) : InplaceButton(tooltip, originalIcon, { }) {

    var onClickedListener: ((event: MouseEvent) -> Unit)? = null

    init {
        addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                onClickedListener?.invoke(e)
            }
        })
    }

    override fun paintHover(g: Graphics) {
        paintHover(g, JBUI.CurrentTheme.ActionButton.hoverBackground())
    }

    fun showProgressFor(timeMillis: Long) {
        val listener = onClickedListener
        onClickedListener = null
        icon = AnimatedIcon.Default()

        appCoroutineScope.launch {
            delay(timeMillis)
            onClickedListener = listener
            icon = originalIcon
        }
    }
}
