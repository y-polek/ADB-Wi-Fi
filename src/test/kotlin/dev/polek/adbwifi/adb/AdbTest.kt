package dev.polek.adbwifi.adb

import dev.polek.adbwifi.model.Address
import dev.polek.adbwifi.model.Device
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class AdbTest {

    private val propertiesService = MockPropertiesService()

    @Test
    fun `test multiple devices()`() {
        val commandExecutor = object : MockCommandExecutor(propertiesService.adbLocation) {
            override fun mockOutput(command: String): String = when (command) {
                "$adb devices" -> {
                    """
                    List of devices attached
                    R28M51Y8E0H	device
                    ce0717171c16e33b03	device
                    """.trimIndent()
                }
                "$adb -s R28M51Y8E0H shell getprop ro.serialno" -> "R28M51Y8E0H"
                "$adb -s R28M51Y8E0H shell getprop ro.product.model" -> "SM-G9700"
                "$adb -s R28M51Y8E0H shell getprop ro.product.manufacturer" -> "samsung"
                "$adb -s R28M51Y8E0H shell getprop ro.build.version.release" -> "10"
                "$adb -s R28M51Y8E0H shell getprop ro.build.version.sdk" -> "29"
                "$adb -s R28M51Y8E0H shell ip route" -> {
                    """
                    100.118.14.208/29 dev rmnet_data0 proto kernel scope link src 100.118.15.213
                    192.168.1.0/24 dev wlan0 proto kernel scope link src 192.168.1.179
                    10.0.2.2 dev eth0  scope link
                    216.58.215.110 via 10.0.2.2 dev eth0
                    """.trimIndent()
                }
                "$adb -s ce0717171c16e33b03 shell getprop ro.serialno" -> "ce0717171c16e33b03"
                "$adb -s ce0717171c16e33b03 shell getprop ro.product.model" -> "SM-G930F"
                "$adb -s ce0717171c16e33b03 shell getprop ro.product.manufacturer" -> "samsung"
                "$adb -s ce0717171c16e33b03 shell getprop ro.build.version.release" -> "8.0.0"
                "$adb -s ce0717171c16e33b03 shell getprop ro.build.version.sdk" -> "26"
                "$adb -s ce0717171c16e33b03 shell ip route" -> {
                    """
                    192.168.1.0/24 dev wlan0  proto kernel  scope link  src 192.168.1.159
                    """.trimIndent()
                }
                else -> throw NotImplementedError("Unknown command: '$command'")
            }
        }

        val devices = Adb(commandExecutor, propertiesService).devices()

        assertThat(devices).hasSize(3)

        val device1 = devices[0]
        assertThat(device1.id).isEqualTo("ce0717171c16e33b03")
        assertThat(device1.serialNumber).isEqualTo("ce0717171c16e33b03")
        assertThat(device1.name).isEqualTo("samsung SM-G930F")
        assertThat(device1.address).isEqualTo(Address("wlan0", "192.168.1.159"))
        assertThat(device1.androidVersion).isEqualTo("8.0.0")
        assertThat(device1.apiLevel).isEqualTo("26")
        assertThat(device1.connectionType).isEqualTo(Device.ConnectionType.USB)
        assertThat(device1.isConnected).isFalse()

        val device2 = devices[1]
        assertThat(device2.id).isEqualTo("R28M51Y8E0H")
        assertThat(device2.serialNumber).isEqualTo("R28M51Y8E0H")
        assertThat(device2.name).isEqualTo("samsung SM-G9700")
        assertThat(device2.address).isEqualTo(Address("wlan0", "192.168.1.179"))
        assertThat(device2.androidVersion).isEqualTo("10")
        assertThat(device2.apiLevel).isEqualTo("29")
        assertThat(device2.connectionType).isEqualTo(Device.ConnectionType.USB)
        assertThat(device2.isConnected).isFalse()

        val device3 = devices[2]
        assertThat(device3.id).isEqualTo("R28M51Y8E0H")
        assertThat(device3.serialNumber).isEqualTo("R28M51Y8E0H")
        assertThat(device3.name).isEqualTo("samsung SM-G9700")
        assertThat(device3.address).isEqualTo(Address("rmnet_data0", "100.118.15.213"))
        assertThat(device3.androidVersion).isEqualTo("10")
        assertThat(device3.apiLevel).isEqualTo("29")
        assertThat(device3.connectionType).isEqualTo(Device.ConnectionType.USB)
        assertThat(device3.isConnected).isFalse()
    }

    @Test
    fun `test address parsing`() {
        val commandExecutor = MockCommandExecutor(propertiesService.adbLocation) { command ->
            when (command) {
                "$adb -s R28M51Y8E0H shell ip route" -> {
                    """
                    100.106.57.0/29 dev rmnet_data0 proto kernel scope link src 100.106.57.3
                    192.168.1.0/24 dev wlan0 proto kernel scope link src 192.168.1.188
                    192.168.43.0/24 dev swlan0 proto kernel scope link src 192.168.43.1
                    """.trimIndent()
                }
                else -> throw NotImplementedError("Unknown command: '$command'")
            }
        }
        val adb = Adb(commandExecutor, propertiesService)

        val addresses = adb.addresses("R28M51Y8E0H")

        assertThat(addresses).containsExactly(
            Address("swlan0", "192.168.43.1"),
            Address("wlan0", "192.168.1.188"),
            Address("rmnet_data0", "100.106.57.3")
        )
    }

    @Test
    fun `test USB device with multiple IP addresses`() {
        val commandExecutor = object : MockCommandExecutor(propertiesService.adbLocation) {
            override fun mockOutput(command: String): String = when (command) {
                "$adb devices" -> {
                    """
                    List of devices attached
                    R28M51Y8E0H	device
                    """.trimIndent()
                }
                "$adb -s R28M51Y8E0H shell getprop ro.serialno" -> "R28M51Y8E0H"
                "$adb -s R28M51Y8E0H shell getprop ro.product.model" -> "SM-G9700"
                "$adb -s R28M51Y8E0H shell getprop ro.product.manufacturer" -> "samsung"
                "$adb -s R28M51Y8E0H shell getprop ro.build.version.release" -> "10"
                "$adb -s R28M51Y8E0H shell getprop ro.build.version.sdk" -> "29"
                "$adb -s R28M51Y8E0H shell ip route" -> {
                    """
                    100.106.57.0/29 dev rmnet_data0 proto kernel scope link src 100.106.57.3
                    192.168.1.0/24 dev wlan0 proto kernel scope link src 192.168.1.188
                    192.168.43.0/24 dev swlan0 proto kernel scope link src 192.168.43.1
                    """.trimIndent()
                }
                else -> throw NotImplementedError("Unknown command: '$command'")
            }
        }

        val devices = Adb(commandExecutor, propertiesService).devices()

        assertThat(devices).hasSize(3)

        assertThat(devices[0].address).isEqualTo(Address("swlan0", "192.168.43.1"))
        assertThat(devices[1].address).isEqualTo(Address("wlan0", "192.168.1.188"))
        assertThat(devices[2].address).isEqualTo(Address("rmnet_data0", "100.106.57.3"))
    }

    @Test
    fun `test Wi-Fi device with multiple IP addresses`() {
        val commandExecutor = object : MockCommandExecutor(propertiesService.adbLocation) {
            override fun mockOutput(command: String): String = when (command) {
                "$adb devices" -> {
                    """
                    List of devices attached
                    192.168.1.188:5555	device
                    """.trimIndent()
                }
                "$adb -s 192.168.1.188:5555 shell getprop ro.serialno" -> "192.168.1.188:5555"
                "$adb -s 192.168.1.188:5555 shell getprop ro.product.model" -> "SM-G9700"
                "$adb -s 192.168.1.188:5555 shell getprop ro.product.manufacturer" -> "samsung"
                "$adb -s 192.168.1.188:5555 shell getprop ro.build.version.release" -> "10"
                "$adb -s 192.168.1.188:5555 shell getprop ro.build.version.sdk" -> "29"
                "$adb -s 192.168.1.188:5555 shell ip route" -> {
                    """
                    100.106.57.0/29 dev rmnet_data0 proto kernel scope link src 100.106.57.3
                    192.168.1.0/24 dev wlan0 proto kernel scope link src 192.168.1.188
                    192.168.43.0/24 dev swlan0 proto kernel scope link src 192.168.43.1
                    """.trimIndent()
                }
                else -> throw NotImplementedError("Unknown command: '$command'")
            }
        }

        val devices = Adb(commandExecutor, propertiesService).devices()

        assertThat(devices).hasSize(1)

        assertThat(devices[0].address).isEqualTo(Address("wlan0", "192.168.1.188"))
    }

    @Test
    fun `test USB device with no network`() {
        val commandExecutor = object : MockCommandExecutor(propertiesService.adbLocation) {
            override fun mockOutput(command: String): String = when (command) {
                "$adb devices" -> {
                    """
                    List of devices attached
                    R28M51Y8E0H	device
                    """.trimIndent()
                }
                "$adb -s R28M51Y8E0H shell getprop ro.serialno" -> "R28M51Y8E0H"
                "$adb -s R28M51Y8E0H shell getprop ro.product.model" -> "SM-G9700"
                "$adb -s R28M51Y8E0H shell getprop ro.product.manufacturer" -> "samsung"
                "$adb -s R28M51Y8E0H shell getprop ro.build.version.release" -> "10"
                "$adb -s R28M51Y8E0H shell getprop ro.build.version.sdk" -> "29"
                "$adb -s R28M51Y8E0H shell ip route" -> ""
                else -> throw NotImplementedError("Unknown command: '$command'")
            }
        }

        val devices = Adb(commandExecutor, propertiesService).devices()

        assertThat(devices).hasSize(1)

        assertThat(devices[0].address).isNull()
    }
}
