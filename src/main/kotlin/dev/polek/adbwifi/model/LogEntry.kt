package dev.polek.adbwifi.model

sealed class LogEntry(val text: String) {
    class Command(text: String) : LogEntry(text)
    class Output(text: String) : LogEntry(text)
}