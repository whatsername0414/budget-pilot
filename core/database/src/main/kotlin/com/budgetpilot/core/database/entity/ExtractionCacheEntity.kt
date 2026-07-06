package com.budgetpilot.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "extraction_cache")
data class ExtractionCacheEntity(
    @PrimaryKey
    val imageHash: String,
    val resultJson: String,
)
