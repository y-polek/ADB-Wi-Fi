package dev.polek.adbwifi.services

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.util.SystemInfo
import dev.polek.adbwifi.utils.findScrcpyExecInSystemPath
import dev.polek.adbwifi.utils.hasAdbInSystemPath
import dev.polek.adbwifi.utils.isValidAdbLocation
import java.io.File

class PropertiesServiceImpl : PropertiesService {

    private val properties = PropertiesComponent.getInstance()

    override var isLogVisible: Boolean
        get() = properties.getBoolean(IS_LOG_VISIBLE_PROPERTY, false)
        set(value) {
            properties.setValue(IS_LOG_VISIBLE_PROPERTY, value)
        }

    override var isPreviouslyConnectedDevicesExpanded: Boolean
        get() = properties.getBoolean(IS_PREVIOUSLY_CONNECTED_DEVICES_EXPANDED, true)
        set(value) {
            properties.setValue(IS_PREVIOUSLY_CONNECTED_DEVICES_EXPANDED, value, true)
        }

    override var useAdbFromPath: Boolean
        get() = properties.getBoolean(ADB_FROM_SYSTEM_PATH, false)
        set(value) {
            properties.setValue(ADB_FROM_SYSTEM_PATH, value)
            notifyAdbLocationListener()
        }

    override var adbLocation: String
        get() = properties.getValue(ADB_LOCATION_PROPERTY, defaultAdbLocation)
        set(value) {
            properties.setValue(ADB_LOCATION_PROPERTY, value)
            notifyAdbLocationListener()
        }

    override var scrcpyEnabled: Boolean
        get() = properties.getBoolean(SCRCPY_ENABLED, defaultScrcpyEnabled)
        set(value) {
            properties.setValue(SCRCPY_ENABLED, value, defaultScrcpyEnabled)
            notifyAdbLocationListener()
        }

    override val defaultScrcpyEnabled: Boolean
        get() {
            return findScrcpyExecInSystemPath() != null
        }

    override var useScrcpyFromPath: Boolean
        get() = properties.getBoolean(SCRCPY_FROM_SYSTEM_PATH, true)
        set(value) {
            properties.setValue(SCRCPY_FROM_SYSTEM_PATH, value, true)
        }

    override var scrcpyLocation: String
        get() = properties.getValue(SCRCPY_LOCATION_PROPERTY, defaultScrcpyLocation)
        set(value) {
            properties.setValue(SCRCPY_LOCATION_PROPERTY, value)
        }

    override val defaultAdbLocation: String by lazy {
        val home = System.getProperty("user.home")
        val path = when {
            SystemInfo.isMac -> "$home/Library/Android/sdk/platform-tools"
            SystemInfo.isWindows -> "$home/AppData/Local/Android/Sdk/platform-tools"
            else -> "$home/Android/Sdk/platform-tools"
        }
        return@lazy File(path).absolutePath
    }

    override val defaultScrcpyLocation: String = ""

    override var adbLocationListener: ((isValid: Boolean) -> Unit)? = null
        set(value) {
            field = value
            notifyAdbLocationListener()
        }

    private fun notifyAdbLocationListener() {
        val isValid = when {
            useAdbFromPath -> hasAdbInSystemPath()
            else -> isValidAdbLocation(adbLocation)
        }
        adbLocationListener?.invoke(isValid)
    }

    override var scrcpyEnabledListener: ((isEnabled: Boolean) -> Unit)? = null
        set(value) {
            field = value
            notifyScrcpyEnabledListener()
        }

    private fun notifyScrcpyEnabledListener() {
        scrcpyEnabledListener?.invoke(scrcpyEnabled)
    }

    private companion object {
        private const val IS_LOG_VISIBLE_PROPERTY = "dev.polek.adbwifi.IS_LOG_VISIBLE_PROPERTY"

        private const val IS_PREVIOUSLY_CONNECTED_DEVICES_EXPANDED =
            "dev.polek.adbwifi.IS_PREVIOUSLY_CONNECTED_DEVICES_EXPANDED"

        private const val ADB_FROM_SYSTEM_PATH = "dev.polek.adbwifi.ADB_FROM_SYSTEM_PATH"
        private const val ADB_LOCATION_PROPERTY = "dev.polek.adbwifi.ADB_LOCATION_PROPERTY"

        private const val SCRCPY_ENABLED = "dev.polek.adbwifi.SCRCPY_ENABLED"
        private const val SCRCPY_FROM_SYSTEM_PATH = "dev.polek.adbwifi.SCRCPY_FROM_SYSTEM_PATH"
        private const val SCRCPY_LOCATION_PROPERTY = "dev.polek.adbwifi.SCRCPY_LOCATION_PROPERTY"
    }
}
