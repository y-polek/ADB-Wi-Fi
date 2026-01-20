package dev.polek.adbwifi.model

import com.intellij.openapi.application.EDT
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.util.*

class CommandHistory {

    private val logEntries = LinkedList<LogEntry>()

    private val _entries = MutableStateFlow<List<LogEntry>>(emptyList())
    val entries: StateFlow<List<LogEntry>> = _entries.asStateFlow()

    suspend fun add(entry: LogEntry) = withContext(Dispatchers.EDT) {
        logEntries.add(entry)
        ensureCapacity()
        _entries.value = logEntries.toList()
    }

    fun clear() {
        logEntries.clear()
        _entries.value = emptyList()
    }

    private fun ensureCapacity() {
        if (logEntries.size > CAPACITY) {
            repeat(logEntries.size - CAPACITY) {
                logEntries.removeFirst()
            }
        }
    }

    private companion object {
        private const val CAPACITY = 100
    }
}
