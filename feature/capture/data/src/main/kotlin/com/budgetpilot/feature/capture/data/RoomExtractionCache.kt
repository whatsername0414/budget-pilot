package com.budgetpilot.feature.capture.data

import com.budgetpilot.core.database.dao.ExtractionCacheDao
import com.budgetpilot.core.database.entity.ExtractionCacheEntity
import com.budgetpilot.feature.capture.domain.ExtractionCache

class RoomExtractionCache(
    private val dao: ExtractionCacheDao,
) : ExtractionCache {
    override suspend fun get(imageHash: String): String? = dao.getByHash(imageHash)?.resultJson

    override suspend fun put(
        imageHash: String,
        resultJson: String,
    ) {
        dao.upsert(ExtractionCacheEntity(imageHash = imageHash, resultJson = resultJson))
    }
}
