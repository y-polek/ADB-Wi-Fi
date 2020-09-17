package dev.polek.adbwifi.ui.view

import com.intellij.icons.AllIcons
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.panels.OpaquePanel
import dev.polek.adbwifi.ui.model.DeviceViewModel
import dev.polek.adbwifi.ui.presenter.ToolWindowPresenter
import dev.polek.adbwifi.utils.removeIf
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import javax.swing.BoxLayout

class DeviceListPanel(
    presenter: ToolWindowPresenter,
    private val title: String,
    private var isExpanded: Boolean
) : JBPanel<DeviceListPanel>() {

    var devices: List<DeviceViewModel> = emptyList()
        set(value) {
            field = value
            rebuildUi()
        }

    private val devicePanelListener = object : DevicePanel.Listener {

        override fun onConnectButtonClicked(device: DeviceViewModel) {
            presenter.onConnectButtonClicked(device)
        }

        override fun onDisconnectButtonClicked(device: DeviceViewModel) {
            presenter.onDisconnectButtonClicked(device)
        }

        override fun onPinButtonClicked(device: DeviceViewModel) {
            presenter.onPinButtonClicked(device)
        }

        override fun onShareScreenClicked(device: DeviceViewModel) {
            presenter.onShareScreenButtonClicked(device)
        }

        override fun onCopyDeviceIdClicked(device: DeviceViewModel) {
            presenter.onCopyDeviceIdClicked(device)
        }

        override fun onCopyDeviceAddressClicked(device: DeviceViewModel) {
            presenter.onCopyDeviceAddressClicked(device)
        }
    }

    init {
        layout = BoxLayout(this, BoxLayout.PAGE_AXIS)
        background = JBColor.background()

        buildHeader()
        rebuildUi()
    }

    private fun buildHeader() {
        val header = OpaquePanel(GridBagLayout())

        val headerLabel = JBLabel(title)
        headerLabel.foreground = HEADER_FOREGROUND_COLOR
        header.add(
            headerLabel,
            GridBagConstraints().apply {
                gridx = 0
                gridy = 0
                weightx = 1.0
                fill = GridBagConstraints.HORIZONTAL
                insets = Insets(0, 10, 0, 0)
            }
        )

        val headerIcon = JBLabel(AllIcons.General.ArrowUp)
        header.add(
            headerIcon,
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
    }

    private fun rebuildUi() {
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
    }
}
