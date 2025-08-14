package com.rio.rostry.core.database.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.rio.rostry.core.database.entities.SyncMetadata
import com.rio.rostry.core.database.entities.SyncStatus
import com.rio.rostry.core.database.entities.SyncPriority
import com.rio.rostry.core.common.model.TransactionStatus
import com.rio.rostry.core.common.model.TransactionType
import java.util.*

/**
 * Room type converters for complex data types
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

class SyncMetadataConverter {
    private val gson = Gson()

    @TypeConverter
    fun fromSyncMetadata(syncMetadata: SyncMetadata): String {
        return gson.toJson(syncMetadata)
    }

    @TypeConverter
    fun toSyncMetadata(syncMetadataString: String): SyncMetadata {
        return gson.fromJson(syncMetadataString, SyncMetadata::class.java)
    }
}

class TransactionStatusConverter {
    @TypeConverter
    fun fromTransactionStatus(status: TransactionStatus): String {
        return status.name
    }

    @TypeConverter
    fun toTransactionStatus(status: String): TransactionStatus {
        return TransactionStatus.valueOf(status)
    }
}

class TransactionTypeConverter {
    @TypeConverter
    fun fromTransactionType(type: TransactionType): String {
        return type.name
    }

    @TypeConverter
    fun toTransactionType(type: String): TransactionType {
        return TransactionType.valueOf(type)
    }
}

class SyncStatusConverter {
    @TypeConverter
    fun fromSyncStatus(status: SyncStatus): String {
        return status.name
    }

    @TypeConverter
    fun toSyncStatus(status: String): SyncStatus {
        return SyncStatus.valueOf(status)
    }
}

class SyncPriorityConverter {
    @TypeConverter
    fun fromSyncPriority(priority: SyncPriority): String {
        return priority.name
    }

    @TypeConverter
    fun toSyncPriority(priority: String): SyncPriority {
        return SyncPriority.valueOf(priority)
    }
}

class StringListConverter {
    private val gson = Gson()

    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, listType) ?: emptyList()
    }
}