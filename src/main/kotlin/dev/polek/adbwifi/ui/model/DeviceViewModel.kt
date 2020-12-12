package dev.polek.adbwifi.ui.model

import com.intellij.openapi.util.IconLoader
import dev.polek.adbwifi.model.Address
import dev.polek.adbwifi.model.Device
import dev.polek.adbwifi.model.Device.ConnectionType.*
import dev.polek.adbwifi.model.PinnedDevice
import javax.swing.Icon

data class DeviceViewModel(
    val device: Device,
    val titleText: String,
    val subtitleText: String,
    val subtitleIcon: Icon?,
    val icon: Icon,
    val hasAddress: Boolean,
    val buttonType: ButtonType,
    var isShareScreenButtonVisible: Boolean,
    val isRemoveButtonVisible: Boolean,
    var isInProgress: Boolean = false
) {
    val id: String
        get() = device.id

    val serialNumber: String
        get() = device.serialNumber

    val address: String?
        get() = device.address?.ip

    val uniqueId: String
        get() = device.uniqueId

    enum class ButtonType {
        CONNECT, CONNECT_DISABLED, DISCONNECT
    }

    companion object {
        private val ICON_USB = IconLoader.getIcon("/icons/usbIcon.svg")
        private val ICON_NO_USB = IconLoader.getIcon("/icons/noUsbIcon.svg")
        private val ICON_WIFI = IconLoader.getIcon("/icons/wifiIcon.svg")
        private val ICON_NO_WIFI = IconLoader.getIcon("/icons/noWifi.svg")
        private val ICON_WIFI_NETWORK = IconLoader.getIcon("/icons/wifiNetwork.svg")
        private val ICON_MOBILE_NETWORK = IconLoader.getIcon("/icons/mobileNetwork.svg")
        private val ICON_HOTSPOT_NETWORK = IconLoader.getIcon("/icons/hotspotNetwork.svg")

        fun Device.toViewModel(): DeviceViewModel {
            val device = this
            return DeviceViewModel(
                device = device,
                titleText = device.name,
                subtitleText = device.subtitleText(),
                subtitleIcon = device.addressIcon(),
                icon = device.icon(),
                hasAddress = device.hasAddress(),
                buttonType = device.buttonType(),
                isShareScreenButtonVisible = false,
                isRemoveButtonVisible = false
            )
        }

        fun PinnedDevice.toViewModel(): DeviceViewModel {
            val device = Device(
                id = this.id,
                serialNumber = this.serialNumber,
                name = this.name,
                address = Address("", this.address),
                androidVersion = this.androidVersion,
                apiLevel = this.apiLevel,
                connectionType = NONE,
                isPinnedDevice = true
            )
            return DeviceViewModel(
                device = device,
                titleText = device.name,
                subtitleText = device.subtitleText(),
                subtitleIcon = device.addressIcon(),
                icon = device.icon(),
                hasAddress = device.hasAddress(),
                buttonType = device.buttonType(),
                isShareScreenButtonVisible = false,
                isRemoveButtonVisible = true
            )
        }

        private fun Device.subtitleText() = buildString {
            val device = this@subtitleText
            append("Android ${device.androidVersion} (API ${device.apiLevel}) -")
            if (device.address != null) {
                append(" ${device.address.ip}")
            }
        }

        private fun Device.icon(): Icon = when (connectionType) {
            USB -> ICON_USB
            WIFI -> ICON_WIFI
            NONE -> ICON_NO_USB
        }

        private fun Device.addressIcon(): Icon? {
            address ?: return ICON_NO_WIFI
            if (connectionType != USB) return null
            return when {
                address.isWifiNetwork -> ICON_WIFI_NETWORK
                address.isMobileNetwork -> ICON_MOBILE_NETWORK
                address.isHotspotNetwork -> ICON_HOTSPOT_NETWORK
                else -> null
            }
        }

        private fun Device.hasAddress() = this.address != null

        private fun Device.buttonType(): ButtonType {
            val device = this
            return when {
                device.isWifiDevice -> ButtonType.DISCONNECT
                device.address?.ip.isNullOrBlank() -> ButtonType.CONNECT_DISABLED
                device.isUsbDevice && device.isConnected -> ButtonType.CONNECT_DISABLED
                else -> ButtonType.CONNECT
            }
        }
    }
}
