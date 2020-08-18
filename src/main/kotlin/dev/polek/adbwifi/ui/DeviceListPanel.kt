package dev.polek.adbwifi.ui

import com.intellij.openapi.diagnostic.logger
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBPanel
import dev.polek.adbwifi.model.Device
import javax.swing.BoxLayout

class DeviceListPanel : JBPanel<DeviceListPanel>() {

    var devices: List<Device> = emptyList()
        set(value) {
            val oldList = field
            field = value
            if (!oldList.contentDeepEquals(value)) {
                rebuildUi()
            }
        }

    init {
        layout = BoxLayout(this, BoxLayout.PAGE_AXIS)
        background = JBColor.background()
        rebuildUi()
    }

    private fun rebuildUi() {
        logger("AdbService").info("rebuildUi ${devices.size}")
        removeAll()
        devices.forEach { device ->
            add(DevicePanel(device))
        }
        revalidate()
        repaint()
    }

    private companion object {
        private fun List<Device>.contentDeepEquals(other: List<Device>): Boolean {
            return this.toTypedArray().contentDeepEquals(other.toTypedArray())
        }
    }
}
