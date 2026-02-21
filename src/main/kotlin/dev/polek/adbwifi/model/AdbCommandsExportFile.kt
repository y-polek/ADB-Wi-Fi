package dev.polek.adbwifi.model

import com.google.gson.JsonArray
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import dev.polek.adbwifi.model.CommandListItem.AdbCommand
import dev.polek.adbwifi.model.CommandListItem.AdbCommandGroup
import java.lang.reflect.Type

data class AdbCommandsExportFile(
    val version: Int = 1,
    val commands: List<ExportItem>
)

sealed interface ExportItem

data class ExportCommand(
    val name: String,
    val command: String,
    val iconId: String = "",
    val iconColor: String = "primary",
    val isEnabled: Boolean = true,
    val requiresConfirmation: Boolean = false
) : ExportItem

data class ExportGroup(
    val name: String,
    val iconId: String = "",
    val iconColor: String = "primary",
    val commands: List<ExportCommand> = emptyList()
) : ExportItem

class ExportItemAdapter : JsonSerializer<ExportItem>, JsonDeserializer<ExportItem> {

    override fun serialize(src: ExportItem, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        return when (src) {
            is ExportCommand -> {
                val obj = context.serialize(src, ExportCommand::class.java).asJsonObject
                addTypeField("command", obj)
            }
            is ExportGroup -> {
                val obj = context.serialize(src, ExportGroup::class.java).asJsonObject
                // Add "type": "command" to each nested command
                val cmds = obj.getAsJsonArray("commands")?.map { element ->
                    addTypeField("command", element.asJsonObject)
                }
                if (cmds != null) {
                    obj.add("commands", JsonArray().apply { cmds.forEach(::add) })
                }
                addTypeField("group", obj)
            }
        }
    }

    private fun addTypeField(type: String, obj: JsonObject): JsonObject {
        val result = JsonObject()
        result.addProperty("type", type)
        obj.entrySet().forEach { (key, value) -> result.add(key, value) }
        return result
    }

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): ExportItem {
        val obj = json.asJsonObject
        return if (obj.get("type")?.asString == "group") {
            obj.getAsJsonArray("commands")?.let { commandsArray ->
                val filtered = commandsArray.filter {
                    it.isJsonObject && it.asJsonObject.get("type")?.asString != "group"
                }
                obj.add("commands", JsonArray().apply { filtered.forEach(::add) })
            }
            context.deserialize(obj, ExportGroup::class.java)
        } else {
            context.deserialize(obj, ExportCommand::class.java)
        }
    }
}

fun exportItemsToCommandListItems(items: List<ExportItem>): List<CommandListItem> {
    return items.map { item ->
        when (item) {
            is ExportCommand -> AdbCommand(
                name = item.name,
                command = item.command,
                iconId = item.iconId,
                iconColor = item.iconColor,
                isEnabled = item.isEnabled,
                requiresConfirmation = item.requiresConfirmation
            )
            is ExportGroup -> AdbCommandGroup(
                name = item.name,
                iconId = item.iconId,
                iconColor = item.iconColor,
                commands = item.commands.map { cmd ->
                    AdbCommand(
                        name = cmd.name,
                        command = cmd.command,
                        iconId = cmd.iconId,
                        iconColor = cmd.iconColor,
                        isEnabled = cmd.isEnabled,
                        requiresConfirmation = cmd.requiresConfirmation
                    )
                }
            )
        }
    }
}

fun commandListItemsToExportItems(items: List<CommandListItem>): List<ExportItem> {
    return items.map { item ->
        when (item) {
            is AdbCommand -> ExportCommand(
                name = item.name,
                command = item.command,
                iconId = item.iconId,
                iconColor = item.iconColor,
                isEnabled = item.isEnabled,
                requiresConfirmation = item.requiresConfirmation
            )
            is AdbCommandGroup -> ExportGroup(
                name = item.name,
                iconId = item.iconId,
                iconColor = item.iconColor,
                commands = item.commands.map { cmd ->
                    ExportCommand(
                        name = cmd.name,
                        command = cmd.command,
                        iconId = cmd.iconId,
                        iconColor = cmd.iconColor,
                        isEnabled = cmd.isEnabled,
                        requiresConfirmation = cmd.requiresConfirmation
                    )
                }
            )
        }
    }
}
