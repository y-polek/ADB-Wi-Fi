package dev.polek.adbwifi.model

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.withContext
import java.util.*

@OptIn(ExperimentalCoroutinesApi::class)
class CommandHistory {

    private val logEntries = LinkedList<LogEntry>()

    var listener: Listener? = null

    suspend fun add(entry: LogEntry) = withContext(Dispatchers.Main) {
        logEntries.add(entry)
        ensureCapacity()
        notifyListener()
    }

    fun getLogEntries(): List<LogEntry> = logEntries

    private fun ensureCapacity() {
        if (logEntries.size > CAPACITY) {
            repeat(logEntries.size - CAPACITY) {
                logEntries.removeFirst()
            }
        }
    }

    private fun notifyListener() {
        listener?.onLogEntriesModified(logEntries)
    }

    interface Listener {
        fun onLogEntriesModified(entries: List<LogEntry>)
    }

    private companion object {
        private const val CAPACITY = 100
    }
}
