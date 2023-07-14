package com.example.todoapp.domain

import androidx.room.TypeConverter
import java.util.Date
/**
 * Provides some converter functions for DB
 */
class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time?.toLong()
    }
}
