package dev.polek.adbwifi.model

import java.util.*

class CommandHistory {

    var listener: Listener? = null

    val commands = LinkedList<Command>()

    private fun add(command: Command) {
        addImpl(command)
        notifyListener()
    }

    fun add(commands: Iterable<Command>) {
        commands.forEach(::addImpl)
        notifyListener()
    }

    operator fun plusAssign(commands: Iterable<Command>) {
        add(commands)
    }

    private fun addImpl(command: Command) {
        commands += command
        if (commands.size > MAX_SIZE) {
            repeat(commands.size - MAX_SIZE) {
                commands.removeFirst()
            }
        }
    }

    private fun notifyListener() {
        listener?.onCommandHistoryModified(commands)
    }

    interface Listener {
        fun onCommandHistoryModified(commands: List<Command>)
    }

    private companion object {
        private const val MAX_SIZE = 10
    }
}
