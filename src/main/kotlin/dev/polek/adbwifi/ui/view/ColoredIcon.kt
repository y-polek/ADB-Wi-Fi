package dev.polek.adbwifi.ui.view

import com.intellij.util.ui.UIUtil
import java.awt.AlphaComposite
import java.awt.Color
import java.awt.Component
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import javax.swing.Icon

class ColoredIcon(private val delegate: Icon, private val color: Color) : Icon {

    override fun paintIcon(c: Component?, g: Graphics?, x: Int, y: Int) {
        val g2 = g as? Graphics2D ?: return
        val image = UIUtil.createImage(c, iconWidth, iconHeight, BufferedImage.TYPE_INT_ARGB)
        val ig = image.createGraphics()

        delegate.paintIcon(c, ig, 0, 0)

        ig.composite = AlphaComposite.SrcIn
        ig.color = color
        ig.fillRect(0, 0, iconWidth, iconHeight)
        ig.dispose()

        UIUtil.drawImage(g2, image, x, y, c)
    }

    override fun getIconWidth(): Int = delegate.iconWidth

    override fun getIconHeight(): Int = delegate.iconHeight
}
