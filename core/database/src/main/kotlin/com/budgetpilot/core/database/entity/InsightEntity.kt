package com.budgetpilot.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "insights")
data class InsightEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: String,
    val message: String,
    val month: String,
    val createdAt: Instant,
    val dismissedAt: Instant? = null,
)
