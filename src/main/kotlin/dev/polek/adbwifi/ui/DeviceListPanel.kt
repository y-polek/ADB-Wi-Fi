package dev.polek.adbwifi.ui

import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.intellij.util.ui.UIUtil
import dev.polek.adbwifi.model.Device
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JPanel

class DeviceListPanel : JBPanel<DeviceListPanel>() {

    var devices: List<Device> = emptyList()
        set(value) {
            field = value
            rebuildUi()
        }

    init {
        layout = BoxLayout(this, BoxLayout.PAGE_AXIS)
        rebuildUi()
    }

    private fun rebuildUi() {
        removeAll()
        devices.forEach { device ->
            add(deviceUi(device))
        }
    }

    private fun deviceUi(device: Device): JPanel {
        val panel = JPanel(GridBagLayout())
        panel.minimumSize = Dimension(0, LIST_ITEM_HEIGHT)
        panel.maximumSize = Dimension(Int.MAX_VALUE, LIST_ITEM_HEIGHT)
        panel.preferredSize = Dimension(0, LIST_ITEM_HEIGHT)

        val nameLabel = JBLabel(device.name)
        nameLabel.componentStyle = UIUtil.ComponentStyle.LARGE
        nameLabel.makeBold()
        panel.add(nameLabel, GridBagConstraints().apply {
            gridx = 0
            gridy = 0
            gridwidth = 2
            gridheight = 1
            fill = GridBagConstraints.BOTH
            anchor = GridBagConstraints.PAGE_START
            weightx = 1.0
            insets = Insets(0, 10, 0, 0)
        })

        val addressLabel = JBLabel(device.address)
        addressLabel.componentStyle = UIUtil.ComponentStyle.SMALL
        addressLabel.fontColor = UIUtil.FontColor.BRIGHTER
        panel.add(panel(top = addressLabel), GridBagConstraints().apply {
            gridx = 0
            gridy = 1
            gridwidth = 3
            gridheight = 1
            fill = GridBagConstraints.BOTH
            anchor = GridBagConstraints.PAGE_END
            weightx = 1.0
            weighty = 1.0
            insets = Insets(0, 10, 0, 10)
        })

        val button = JButton("Connect")
        button.addActionListener {

        }
        panel.add(button, GridBagConstraints().apply {
            gridx = 2
            gridy = 0
            gridwidth = 1
            gridheight = 1
            anchor = GridBagConstraints.PAGE_START
            insets = Insets(5, 10, 0, 10)
        })

        return panel
    }

    companion object {
        private const val LIST_ITEM_HEIGHT = 60
    }
}
