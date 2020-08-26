package dev.polek.adbwifi.ui.model

import com.intellij.openapi.util.IconLoader
import dev.polek.adbwifi.model.Device
import dev.polek.adbwifi.model.Device.ConnectionType
import javax.swing.Icon

data class DeviceViewModel(
    val device: Device,
    val titleText: String,
    val subtitleText: String,
    val icon: Icon,
    val hasAddress: Boolean,
    val buttonType: ButtonType,
    val isPinned: Boolean,
    var isInProgress: Boolean = false
) {
    val id: String
        get() = device.id

    val androidId: String
        get() = device.androidId

    enum class ButtonType {
        CONNECT, CONNECT_DISABLED, DISCONNECT
    }

    companion object {
        private val ICON_USB = IconLoader.getIcon("/icons/usbIcon.svg")
        private val ICON_WIFI = IconLoader.getIcon("/icons/wifiIcon.svg")

        fun Device.toViewModel(): DeviceViewModel {
            val device = this
            val subtitleText = buildString {
                append("Android ${device.androidVersion} (API ${device.apiLevel})")
                if (device.address != null) {
                    append(" - ${device.address}")
                }
            }
            val icon = when (device.connectionType) {
                ConnectionType.USB -> ICON_USB
                ConnectionType.WIFI -> ICON_WIFI
            }
            val buttonType = when {
                device.isWifiDevice -> ButtonType.DISCONNECT
                device.isUsbDevice && device.isConnected -> ButtonType.CONNECT_DISABLED
                else -> ButtonType.CONNECT
            }
            return DeviceViewModel(
                device = device,
                titleText = device.name,
                subtitleText = subtitleText,
                icon = icon,
                hasAddress = device.address != null,
                buttonType = buttonType,
                isPinned = false
            )
        }
    }
}
