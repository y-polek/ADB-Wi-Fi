package dev.polek.adbwifi.ui.model

import dev.polek.adbwifi.model.Device
import dev.polek.adbwifi.model.Device.ConnectionType.*
import dev.polek.adbwifi.model.PinnedDevice
import dev.polek.adbwifi.model.PinnedDevice.Companion.toDevice
import dev.polek.adbwifi.utils.Icons
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

        fun Device.toViewModel(
            customName: String?,
            isRemoveButtonVisible: Boolean = false
        ): DeviceViewModel {
            val device = this
            return DeviceViewModel(
                device = device,
                titleText = customName ?: device.name,
                subtitleText = device.subtitleText(),
                subtitleIcon = device.addressIcon(),
                icon = device.icon(),
                hasAddress = device.hasAddress(),
                buttonType = device.buttonType(),
                isShareScreenButtonVisible = false,
                isRemoveButtonVisible = isRemoveButtonVisible
            )
        }

        fun PinnedDevice.toViewModel(customName: String?): DeviceViewModel {
            return toDevice().toViewModel(customName, isRemoveButtonVisible = true)
        }

        private fun Device.subtitleText() = buildString {
            val device = this@subtitleText
            append("<html>")
            append("Android ${device.androidVersion} (API ${device.apiLevel}) -")
            if (device.address != null) {
                append(" <code>${device.address.ip}:${device.port}</code>")
            }
            append("</html>")
        }

        private fun Device.icon(): Icon = when (connectionType) {
            USB -> Icons.USB
            WIFI -> Icons.WIFI
            NONE -> Icons.NO_USB
        }

        private fun Device.addressIcon(): Icon? {
            address ?: return Icons.NO_WIFI
            if (connectionType != USB) return null
            return when {
                address.isWifiNetwork -> Icons.WIFI_NETWORK
                address.isMobileNetwork -> Icons.MOBILE_NETWORK
                address.isHotspotNetwork -> Icons.HOTSPOT_NETWORK
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
