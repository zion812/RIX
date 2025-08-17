package com.rio.rostry.core.database.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*

/**
 * Type converters for Fowl-related entities
 * Handles conversion of complex types like Map and List for Room persistence
 */
class FowlConverters {
    private val gson = Gson()
    
    @TypeConverter
    fun fromStringList(value: List<String>?): String? {
        if (value == null) return null
        return gson.toJson(value)
    }
    
    @TypeConverter
    fun toStringList(value: String?): List<String> {
        if (value == null || value.isEmpty()) return emptyList()
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, listType)
    }
    
    @TypeConverter
    fun fromStringMap(value: Map<String, String>?): String? {
        if (value == null) return null
        return gson.toJson(value)
    }
    
    @TypeConverter
    fun toStringMap(value: String?): Map<String, String> {
        if (value == null || value.isEmpty()) return emptyMap()
        val mapType = object : TypeToken<Map<String, String>>() {}.type
        return gson.fromJson(value, mapType)
    }
    
    @TypeConverter
    fun fromDate(date: Date?): Long? {
        return date?.time
    }
    
    @TypeConverter
    fun toDate(timestamp: Long?): Date? {
        return if (timestamp == null) null else Date(timestamp)
    }
}