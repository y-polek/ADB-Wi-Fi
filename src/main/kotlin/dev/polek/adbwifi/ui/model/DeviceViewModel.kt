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
        get() = device.address

    val uniqueId: String
        get() = device.uniqueId

    enum class ButtonType {
        CONNECT, CONNECT_DISABLED, DISCONNECT
    }

    companion object {
        private val ICON_USB = IconLoader.getIcon("/icons/usbIcon.svg")
        private val ICON_NO_USB = IconLoader.getIcon("/icons/noUsbIcon.svg")
        private val ICON_WIFI = IconLoader.getIcon("/icons/wifiIcon.svg")

        fun Device.toViewModel(): DeviceViewModel {
            val device = this
            return DeviceViewModel(
                device = device,
                titleText = device.name,
                subtitleText = device.subtitleText(),
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
                addresses = listOf(Address("", this.address)),
                androidVersion = this.androidVersion,
                apiLevel = this.apiLevel,
                isPinnedDevice = true
            )
            return DeviceViewModel(
                device = device,
                titleText = device.name,
                subtitleText = device.subtitleText(),
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
                append(" ${device.address}")
            }
        }

        private fun Device.icon() = when (connectionType) {
            USB -> ICON_USB
            WIFI -> ICON_WIFI
            NONE -> ICON_NO_USB
        }

        private fun Device.hasAddress() = this.address != null

        private fun Device.buttonType(): ButtonType {
            val device = this
            return when {
                device.isWifiDevice -> ButtonType.DISCONNECT
                device.address.isNullOrBlank() -> ButtonType.CONNECT_DISABLED
                device.isUsbDevice && device.isConnected -> ButtonType.CONNECT_DISABLED
                else -> ButtonType.CONNECT
            }
        }
    }
}
