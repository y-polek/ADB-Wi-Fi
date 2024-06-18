package dev.polek.adbwifi.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import dev.polek.adbwifi.commandexecutor.CmdResult
import dev.polek.adbwifi.commandexecutor.RuntimeCommandExecutor
import dev.polek.adbwifi.model.Device
import dev.polek.adbwifi.scrcpy.Scrcpy

@Service
class ScrcpyService {

    private val scrcpy = Scrcpy(RuntimeCommandExecutor(), service())

    suspend fun share(device: Device): CmdResult {
        return scrcpy.share(device)
    }

    fun isScrcpyValid() = scrcpy.isValid
}
