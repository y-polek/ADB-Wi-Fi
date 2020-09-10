package dev.polek.adbwifi.ui.view

import com.intellij.ui.JBColor
import com.intellij.ui.components.JBPanel
import dev.polek.adbwifi.ui.model.DeviceViewModel
import dev.polek.adbwifi.ui.presenter.ToolWindowPresenter
import javax.swing.BoxLayout

class DeviceListPanel(presenter: ToolWindowPresenter) : JBPanel<DeviceListPanel>() {

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
        rebuildUi()
    }

    private fun rebuildUi() {
        removeAll()
        devices.forEach { device ->
            val devicePanel = DevicePanel(device)
            devicePanel.listener = devicePanelListener
            add(devicePanel)
        }
        revalidate()
        repaint()
    }
}
