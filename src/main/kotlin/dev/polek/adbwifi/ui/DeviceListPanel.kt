package dev.polek.adbwifi.ui

import com.intellij.ui.JBColor
import com.intellij.ui.components.JBPanel
import dev.polek.adbwifi.model.Device
import javax.swing.BoxLayout

class DeviceListPanel : JBPanel<DeviceListPanel>() {

    var devices: List<Device> = emptyList()
        set(value) {
            field = value
            rebuildUi()
        }

    init {
        layout = BoxLayout(this, BoxLayout.PAGE_AXIS)
        background = JBColor.background()
        rebuildUi()
    }

    private fun rebuildUi() {
        removeAll()
        devices.forEach { device ->
            add(DevicePanel(device))
        }
    }
}
