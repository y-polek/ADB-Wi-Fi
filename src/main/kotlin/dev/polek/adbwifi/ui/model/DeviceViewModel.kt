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
    val icon: Icon?,
    val hasAddress: Boolean,
    val buttonType: ButtonType,
    var isShareScreenButtonVisible: Boolean,
    val isRemoveButtonVisible: Boolean,
    var isInProgress: Boolean = false,
    var packageName: String? = null,
    var isAdbCommandsButtonVisible: Boolean = false,
    val deviceType: DeviceType = DeviceType.PHYSICAL,
    val isConnected: Boolean = false
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

    enum class DeviceType {
        EMULATOR, PHYSICAL
    }

    companion object {

        fun Device.toViewModel(
            customName: String?,
            isRemoveButtonVisible: Boolean = false
        ): DeviceViewModel {
            val device = this
            val deviceType = device.detectDeviceType()
            return DeviceViewModel(
                device = device,
                titleText = customName ?: device.name,
                subtitleText = device.subtitleText(),
                subtitleIcon = device.addressIcon(),
                icon = device.icon(deviceType, isRemoveButtonVisible),
                hasAddress = device.hasAddress(),
                buttonType = device.buttonType(deviceType),
                isShareScreenButtonVisible = false,
                isRemoveButtonVisible = isRemoveButtonVisible,
                deviceType = deviceType,
                isConnected = device.isWifiDevice
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

        private fun Device.detectDeviceType(): DeviceType {
            return when {
                id.startsWith("emulator-") -> DeviceType.EMULATOR
                name.contains("emulator", ignoreCase = true) -> DeviceType.EMULATOR
                name.contains("sdk", ignoreCase = true) -> DeviceType.EMULATOR
                serialNumber.contains("emulator", ignoreCase = true) -> DeviceType.EMULATOR
                else -> DeviceType.PHYSICAL
            }
        }

        private fun Device.icon(deviceType: DeviceType, isPreviouslyConnected: Boolean): Icon? {
            // Previously connected device - no icon
            if (isPreviouslyConnected) return null

            // Emulator - use phone icon
            if (deviceType == DeviceType.EMULATOR) return Icons.PHONE

            // Physical device - based on connection type
            return when (connectionType) {
                USB -> Icons.USB
                WIFI -> Icons.WIFI
                NONE -> null
            }
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

        private fun Device.buttonType(deviceType: DeviceType): ButtonType {
            val device = this
            return when {
                device.isWifiDevice -> ButtonType.DISCONNECT
                deviceType == DeviceType.EMULATOR -> ButtonType.CONNECT_DISABLED
                device.address?.ip.isNullOrBlank() -> ButtonType.CONNECT_DISABLED
                device.isUsbDevice && device.isConnected -> ButtonType.CONNECT_DISABLED
                else -> ButtonType.CONNECT
            }
        }
    }
}
