package com.budgetpilot.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.budgetpilot.core.database.entity.ExtractionCacheEntity

@Dao
interface ExtractionCacheDao {
    @Query("SELECT * FROM extraction_cache WHERE imageHash = :imageHash")
    suspend fun getByHash(imageHash: String): ExtractionCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: ExtractionCacheEntity)
}
