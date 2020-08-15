package dev.polek.adbwifi.model

import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.util.*

@OptIn(ExperimentalCoroutinesApi::class)
class CommandHistory {

    val commands = LinkedList<Command>()
    var listener: Listener? = null

    private fun add(command: Command) {
        addImpl(command)
    }

    fun add(commands: List<Command>) {
        addImpl(*commands.toTypedArray())
    }

    operator fun plusAssign(commands: List<Command>) {
        add(commands)
    }

    private fun addImpl(vararg newCommands: Command) {
        commands += newCommands
        if (commands.size > MAX_SIZE) {
            repeat(commands.size - MAX_SIZE) {
                commands.removeFirst()
            }
        }
        notifyListener()
    }

    private fun notifyListener() {
        listener?.onCommandHistoryModified(commands)
    }

    interface Listener {
        fun onCommandHistoryModified(commands: List<Command>)
    }

    private companion object {
        private const val MAX_SIZE = 100
    }
}
