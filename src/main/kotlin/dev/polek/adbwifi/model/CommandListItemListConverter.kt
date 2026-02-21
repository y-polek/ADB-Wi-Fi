package dev.polek.adbwifi.model

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.google.gson.reflect.TypeToken
import com.intellij.util.xmlb.Converter
import dev.polek.adbwifi.model.CommandListItem.AdbCommand
import dev.polek.adbwifi.model.CommandListItem.AdbCommandGroup
import java.lang.reflect.Type

class CommandListItemListConverter : Converter<List<CommandListItem>>() {

    override fun toString(value: List<CommandListItem>): String? {
        return gson.toJson(value, listType)
    }

    override fun fromString(value: String): List<CommandListItem>? {
        return gson.fromJson(value, listType)
    }

    private companion object {
        private val gson = GsonBuilder()
            .registerTypeAdapter(CommandListItem::class.java, CommandListItemAdapter())
            .create()
        private val listType = object : TypeToken<ArrayList<CommandListItem>>() {}.type
    }
}

class CommandListItemAdapter : JsonSerializer<CommandListItem>, JsonDeserializer<CommandListItem> {

    override fun serialize(src: CommandListItem, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        return when (src) {
            is AdbCommand -> {
                val obj = context.serialize(src, AdbCommand::class.java).asJsonObject
                addTypeField("command", obj)
            }
            is AdbCommandGroup -> {
                val obj = context.serialize(src, AdbCommandGroup::class.java).asJsonObject
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

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): CommandListItem {
        val obj = json.asJsonObject
        return if (obj.get("type")?.asString == "group") {
            obj.getAsJsonArray("commands")?.let { commandsArray ->
                val filtered = commandsArray.filter {
                    it.isJsonObject && it.asJsonObject.get("type")?.asString != "group"
                }
                obj.add("commands", JsonArray().apply { filtered.forEach(::add) })
            }
            context.deserialize(obj, AdbCommandGroup::class.java)
        } else {
            context.deserialize(obj, AdbCommand::class.java)
        }
    }
}
