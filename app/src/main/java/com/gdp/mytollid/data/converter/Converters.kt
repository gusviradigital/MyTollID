package com.gdp.mytollid.data.converter

import androidx.room.TypeConverter
import com.gdp.mytollid.data.entity.CardCategory
import java.util.Date

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromCardCategory(value: CardCategory): String {
        return value.name
    }

    @TypeConverter
    fun toCardCategory(value: String): CardCategory {
        return try {
            CardCategory.valueOf(value)
        } catch (e: IllegalArgumentException) {
            CardCategory.OTHER
        }
    }
} 