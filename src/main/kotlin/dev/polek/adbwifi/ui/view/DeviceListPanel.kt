package dev.polek.adbwifi.ui.view

import com.intellij.icons.AllIcons
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.panels.OpaquePanel
import dev.polek.adbwifi.ui.model.DeviceViewModel
import dev.polek.adbwifi.utils.AbstractMouseListener
import dev.polek.adbwifi.utils.removeIf
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import java.awt.event.MouseEvent
import javax.swing.BorderFactory
import javax.swing.BoxLayout
import javax.swing.JPanel

class DeviceListPanel(
    private val devicePanelListener: DevicePanel.Listener,
    showHeader: Boolean = false,
    private val title: String? = null,
    isHeaderExpanded: Boolean = true,
    private val onHeaderExpandChanged: ((isExpanded: Boolean) -> Unit)? = null
) : JBPanel<DeviceListPanel>() {

    var devices: List<DeviceViewModel> = emptyList()
        set(value) {
            field = value
            rebuildUi()
        }

    private var isExpanded: Boolean = isHeaderExpanded
        set(value) {
            field = value
            rebuildUi()
            onHeaderExpandChanged?.invoke(value)
        }

    private var header: JPanel? = null
    private var headerLabel: JBLabel? = null
    private var headerIcon: JBLabel? = null

    init {
        layout = BoxLayout(this, BoxLayout.PAGE_AXIS)
        background = JBColor.background()

        if (showHeader) {
            buildHeader()
        }
        rebuildUi()
    }

    private fun buildHeader() {
        val header = OpaquePanel(GridBagLayout())
        this.header = header

        headerLabel = JBLabel().apply {
            foreground = HEADER_FOREGROUND_COLOR
        }
        header.add(
            headerLabel!!,
            GridBagConstraints().apply {
                gridx = 0
                gridy = 0
                weightx = 1.0
                fill = GridBagConstraints.HORIZONTAL
                insets = Insets(0, 10, 0, 0)
            }
        )

        headerIcon = JBLabel(AllIcons.General.ArrowUp)
        header.add(
            headerIcon!!,
            GridBagConstraints().apply {
                gridx = 1
                gridy = 0
                insets = Insets(0, 0, 0, 10)
            }
        )

        header.minimumSize = Dimension(0, HEADER_HEIGHT)
        header.maximumSize = Dimension(Int.MAX_VALUE, HEADER_HEIGHT)
        header.preferredSize = Dimension(0, HEADER_HEIGHT)
        header.background = HEADER_BACKGROUND_COLOR
        add(header)

        header.addMouseListener(object : AbstractMouseListener() {
            override fun mouseClicked(e: MouseEvent) {
                isExpanded = !isExpanded
            }
        })
    }

    private fun rebuildUi() {
        header?.border = if (isExpanded) HEADER_BORDER_EXPANDED else HEADER_BORDER_COLLAPSED
        headerLabel?.text = "$title (${devices.size})"
        headerIcon?.icon = if (isExpanded) ICON_EXPANDED else ICON_COLLAPSED

        removeIf { child -> child is DevicePanel }

        if (isExpanded) {
            for (device in devices) {
                val devicePanel = DevicePanel(device)
                devicePanel.listener = devicePanelListener
                add(devicePanel)
            }
        }
        revalidate()
        repaint()
    }

    private companion object {
        private const val HEADER_HEIGHT = 28
        private val HEADER_BACKGROUND_COLOR = JBColor.namedColor(
            "Plugins.lightSelectionBackground",
            JBColor(0xF5F9FF, 0x36393B)
        )
        private val HEADER_FOREGROUND_COLOR = JBColor(0x787878, 0xBBBBBB)

        private val HEADER_BORDER_EXPANDED = BorderFactory.createMatteBorder(1, 0, 0, 0, JBColor.border())
        private val HEADER_BORDER_COLLAPSED = BorderFactory.createMatteBorder(1, 0, 1, 0, JBColor.border())

        private val ICON_EXPANDED = AllIcons.General.ArrowUp
        private val ICON_COLLAPSED = AllIcons.General.ArrowDown
    }
}
