package com.rio.rostry.core.database.converters

import androidx.room.TypeConverter
import java.util.*

/**
 * Simple type converters for Room database
 */
class DateConverter {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}
