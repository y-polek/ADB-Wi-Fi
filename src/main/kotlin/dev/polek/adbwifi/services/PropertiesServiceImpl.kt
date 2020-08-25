package dev.polek.adbwifi.services

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.util.SystemInfo
import java.io.File

class PropertiesServiceImpl : PropertiesService {

    private val properties = PropertiesComponent.getInstance()

    override var isLogVisible: Boolean
        get() {
            return properties.getBoolean(IS_LOG_VISIBLE_PROPERTY, false)
        }
        set(value) {
            properties.setValue(IS_LOG_VISIBLE_PROPERTY, value)
        }

    override var adbLocation: String
        get() {
            return properties.getValue(ADB_LOCATION_PROPERTY, defaultAdbLocation)
        }
        set(value) {
            properties.setValue(ADB_LOCATION_PROPERTY, value)
        }

    private companion object {
        private const val IS_LOG_VISIBLE_PROPERTY = "dev.polek.adbwifi.IS_LOG_VISIBLE_PROPERTY"
        private const val ADB_LOCATION_PROPERTY = "dev.polek.adbwifi.ADB_LOCATION_PROPERTY"

        private val defaultAdbLocation by lazy {
            val home = System.getProperty("user.home")
            val path = when {
                SystemInfo.isMac -> "$home/Library/Android/sdk/platform-tools"
                SystemInfo.isWindows -> "$home/AppData/Local/Android/Sdk/platform-tools"
                else -> "$home/Android/Sdk/platform-tools"
            }
            return@lazy File(path).absolutePath
        }
    }
}
