package com.group2.cleancity.data.db

import androidx.room.TypeConverter
import org.json.JSONArray

class Converters {
    @TypeConverter
    fun fromStringList(value: String?): MutableList<String> {
        val list = mutableListOf<String>()
        if (value.isNullOrBlank()) return list
        try {
            val jsonArray = JSONArray(value)
            for (i in 0 until jsonArray.length()) {
                jsonArray.optString(i)?.let { list.add(it) }
            }
        } catch (e: Exception) {
            value.split("|||").map { it.trim() }.filter { it.isNotEmpty() }.forEach { list.add(it) }
        }
        return list
    }

    @TypeConverter
    fun toStringList(list: MutableList<String>?): String {
        if (list.isNullOrEmpty()) return "[]"
        val jsonArray = JSONArray()
        for (item in list) {
            jsonArray.put(item)
        }
        return jsonArray.toString()
    }
}
