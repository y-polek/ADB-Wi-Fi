package dev.polek.adbwifi.services

import com.intellij.openapi.Disposable

class ShellService : Disposable {

    var isShellVisible = false
        set(value) {
            field = value
            shellVisibilityListener?.invoke(value)
        }

    var shellVisibilityListener: ((isVisible: Boolean) -> Unit)? = null

    override fun dispose() {

    }
}
