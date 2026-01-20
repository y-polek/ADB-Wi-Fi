package dev.polek.adbwifi.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import dev.polek.adbwifi.model.CommandHistory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Service
class LogService {

    private val properties = service<PropertiesService>()

    private val _isLogVisible = MutableStateFlow(properties.isLogVisible)
    val isLogVisible: StateFlow<Boolean> = _isLogVisible.asStateFlow()

    fun setLogVisible(visible: Boolean) {
        _isLogVisible.value = visible
        properties.isLogVisible = visible
    }

    private val _isLogWrapContent = MutableStateFlow(properties.isLogWrapContent)
    val isLogWrapContent: StateFlow<Boolean> = _isLogWrapContent.asStateFlow()

    fun setLogWrapContent(wrap: Boolean) {
        _isLogWrapContent.value = wrap
        properties.isLogWrapContent = wrap
    }

    val commandHistory = CommandHistory()
}
