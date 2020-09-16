package dev.polek.adbwifi.services

import com.intellij.openapi.components.service
import dev.polek.adbwifi.commandexecutor.RuntimeCommandExecutor
import dev.polek.adbwifi.model.Device
import dev.polek.adbwifi.scrcpy.Scrcpy

class ScrcpyService {

    private val scrcpy = Scrcpy(RuntimeCommandExecutor(), service())

    fun share(device: Device) {
        scrcpy.share(device)
    }
}
