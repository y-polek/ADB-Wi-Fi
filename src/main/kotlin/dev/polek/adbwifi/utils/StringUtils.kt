package dev.polek.adbwifi.utils

fun String.startsWithAny(vararg prefixes: String, ignoreCase: Boolean): Boolean {
    for (prefix in prefixes) {
        if (this.startsWith(prefix, ignoreCase = ignoreCase)) return true
    }
    return false
}
