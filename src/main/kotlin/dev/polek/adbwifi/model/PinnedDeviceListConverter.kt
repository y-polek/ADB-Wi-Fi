package dev.polek.adbwifi.model

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.intellij.util.xmlb.Converter
import java.util.ArrayList

class PinnedDeviceListConverter : Converter<List<PinnedDevice>>() {

    override fun toString(value: List<PinnedDevice>): String? {
        return gson.toJson(value)
    }

    override fun fromString(value: String): List<PinnedDevice>? {
        return gson.fromJson(value, listType)
    }

    private companion object {
        private val gson = Gson()
        private val listType = object : TypeToken<ArrayList<PinnedDevice>>() {}.type
    }
}
