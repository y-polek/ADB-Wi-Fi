package dev.polek.adbwifi.services

import com.google.gson.GsonBuilder
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.diagnostic.logger
import com.intellij.util.xmlb.XmlSerializerUtil
import com.intellij.util.xmlb.annotations.OptionTag
import dev.polek.adbwifi.model.*
import dev.polek.adbwifi.model.CommandListItem.AdbCommand
import dev.polek.adbwifi.model.CommandListItem.AdbCommandGroup

@State(
    name = "AdbCommandsService",
    storages = [Storage("adbWifiCommands.xml")]
)
class AdbCommandsService : PersistentStateComponent<AdbCommandsService> {

    @OptionTag(converter = CommandListItemListConverter::class)
    var items: List<CommandListItem> = defaultItems

    fun getEnabledItems(): List<CommandListItem> {
        return items.mapNotNull { item ->
            when (item) {
                is AdbCommand -> item.takeIf { it.isEnabled }
                is AdbCommandGroup -> {
                    val enabledCommands = item.commands.filter { it.isEnabled }
                    if (enabledCommands.isNotEmpty()) {
                        item.copy(commands = enabledCommands)
                    } else {
                        null
                    }
                }
            }
        }
    }

    override fun getState() = this

    override fun loadState(state: AdbCommandsService) {
        XmlSerializerUtil.copyBean(state, this)
    }

    companion object {
        private val LOG = logger<AdbCommandsService>()


        val defaultItems: List<CommandListItem> by lazy {
            loadDefaultsFromResource()
        }

        @Suppress("TooGenericExceptionCaught")
        private fun loadDefaultsFromResource(): List<CommandListItem> {
            try {
                val json = AdbCommandsService::class.java
                    .getResourceAsStream("/default-commands.json")
                    ?.bufferedReader()
                    ?.readText()
                    ?: run {
                        LOG.error("default-commands.json resource not found")
                        return emptyList()
                    }

                val gson = GsonBuilder()
                    .registerTypeAdapter(ExportItem::class.java, ExportItemAdapter())
                    .create()
                val exportFile = gson.fromJson(json, AdbCommandsExportFile::class.java)
                return exportItemsToCommandListItems(exportFile.commands)
            } catch (e: Exception) {
                LOG.error("Failed to load default commands", e)
                return emptyList()
            }
        }
    }
}
