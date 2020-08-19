package dev.polek.adbwifi.services

import com.intellij.ide.util.PropertiesComponent

class LogService {

    var isLogVisible = false
        set(value) {
            field = value
            properties.setValue(IS_LOG_VISIBLE_PROPERTY, value)
            logVisibilityListener?.invoke(value)
        }

    var logVisibilityListener: ((isVisible: Boolean) -> Unit)? = null

    private val properties = PropertiesComponent.getInstance()

    init {
        isLogVisible = properties.getBoolean(IS_LOG_VISIBLE_PROPERTY, false)
    }

    private companion object {
        private const val IS_LOG_VISIBLE_PROPERTY = "dev.polek.adbwifi.IS_LOG_VISIBLE_PROPERTY"
    }
}
