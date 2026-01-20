package dev.polek.adbwifi.model

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.intellij.util.xmlb.Converter

class AdbCommandConfigListConverter : Converter<List<AdbCommandConfig>>() {

    override fun toString(value: List<AdbCommandConfig>): String? {
        return gson.toJson(value)
    }

    override fun fromString(value: String): List<AdbCommandConfig>? {
        return gson.fromJson(value, listType)
    }

    private companion object {
        private val gson = Gson()
        private val listType = object : TypeToken<ArrayList<AdbCommandConfig>>() {}.type
    }
}
