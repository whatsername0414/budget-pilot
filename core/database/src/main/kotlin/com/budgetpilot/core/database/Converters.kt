package com.budgetpilot.core.database

import androidx.room.TypeConverter
import java.time.Instant
import java.time.LocalDate

class Converters {
    // Stored as ISO-8601 text (not epoch day) so sumByMonth can bucket via SQLite's strftime('%Y-%m', date).
    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? = value?.let(LocalDate::parse)

    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? = date?.toString()

    @TypeConverter
    fun toInstant(epochMillis: Long?): Instant? = epochMillis?.let(Instant::ofEpochMilli)

    @TypeConverter
    fun fromInstant(instant: Instant?): Long? = instant?.toEpochMilli()
}
