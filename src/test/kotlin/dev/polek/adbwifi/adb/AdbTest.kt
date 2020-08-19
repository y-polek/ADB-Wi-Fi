package dev.polek.adbwifi.adb

import dev.polek.adbwifi.model.Device
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class AdbTest {

    private val commandExecutor = object : MockCommandExecutor() {
        override fun mockOutput(command: String): String {
            return when (command) {
                "adb devices" -> """
                    List of devices attached
                    R28M51Y8E0H	device
                    ce0717171c16e33b03	device
                """.trimIndent()

                "adb -s R28M51Y8E0H shell getprop ro.product.model" -> "SM-G9700"
                "adb -s R28M51Y8E0H shell getprop ro.product.manufacturer" -> "samsung"
                "adb -s R28M51Y8E0H shell getprop ro.build.version.release" -> "10"
                "adb -s R28M51Y8E0H shell getprop ro.build.version.sdk" -> "29"
                "adb -s R28M51Y8E0H shell ip route" -> "192.168.1.0/24 dev wlan0 proto kernel scope link src 192.168.1.179"

                "adb -s ce0717171c16e33b03 shell getprop ro.product.model" -> "SM-G930F"
                "adb -s ce0717171c16e33b03 shell getprop ro.product.manufacturer" -> "samsung"
                "adb -s ce0717171c16e33b03 shell getprop ro.build.version.release" -> "8.0.0"
                "adb -s ce0717171c16e33b03 shell getprop ro.build.version.sdk" -> "26"
                "adb -s ce0717171c16e33b03 shell ip route" -> "192.168.1.0/24 dev wlan0  proto kernel  scope link  src 192.168.1.159"

                else -> throw NotImplementedError("Unknown command: '$command'")
            }
        }
    }

    private val adb = Adb(commandExecutor)

    @Test
    fun `test devices()`() {
        val devices = adb.devices()

        assertThat(devices).hasSize(2)

        val device1 = devices[0]
        assertThat(device1.id).isEqualTo("ce0717171c16e33b03")
        assertThat(device1.name).isEqualTo("samsung SM-G930F")
        assertThat(device1.address).isEqualTo("192.168.1.159")
        assertThat(device1.androidVersion).isEqualTo("8.0.0")
        assertThat(device1.apiLevel).isEqualTo("26")
        assertThat(device1.connectionType).isEqualTo(Device.ConnectionType.USB)
        assertThat(device1.isConnected).isFalse()

        val device2 = devices[1]
        assertThat(device2.id).isEqualTo("R28M51Y8E0H")
        assertThat(device2.name).isEqualTo("samsung SM-G9700")
        assertThat(device2.address).isEqualTo("192.168.1.179")
        assertThat(device2.androidVersion).isEqualTo("10")
        assertThat(device2.apiLevel).isEqualTo("29")
        assertThat(device2.connectionType).isEqualTo(Device.ConnectionType.USB)
        assertThat(device2.isConnected).isFalse()
    }
}
