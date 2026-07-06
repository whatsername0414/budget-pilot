package com.budgetpilot.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.budgetpilot.core.database.entity.InsightEntity

@Dao
interface InsightDao {
    @Insert
    suspend fun insert(entity: InsightEntity): Long

    @Query("SELECT EXISTS(SELECT 1 FROM insights WHERE type = :type AND month = :month)")
    suspend fun wasShown(
        type: String,
        month: String,
    ): Boolean
}
