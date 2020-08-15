package dev.polek.adbwifi.services

class ShellService {

    var isShellVisible = false
        set(value) {
            field = value
            shellVisibilityListener?.invoke(value)
        }

    var shellVisibilityListener: ((isVisible: Boolean) -> Unit)? = null
}
