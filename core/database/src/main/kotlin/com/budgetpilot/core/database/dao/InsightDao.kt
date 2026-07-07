package com.budgetpilot.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.budgetpilot.core.database.entity.InsightEntity
import java.time.Instant

@Dao
interface InsightDao {
    @Insert
    suspend fun insert(entity: InsightEntity): Long

    @Query("SELECT EXISTS(SELECT 1 FROM insights WHERE type = :type AND month = :month)")
    suspend fun wasShown(
        type: String,
        month: String,
    ): Boolean

    @Query("SELECT * FROM insights WHERE dismissedAt IS NULL ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLatestUndismissed(): InsightEntity?

    @Query("UPDATE insights SET dismissedAt = :dismissedAt WHERE id = :id")
    suspend fun dismiss(
        id: Long,
        dismissedAt: Instant,
    )
}
