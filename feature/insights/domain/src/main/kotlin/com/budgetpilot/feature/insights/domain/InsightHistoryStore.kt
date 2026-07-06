package com.budgetpilot.feature.insights.domain

import com.budgetpilot.feature.insights.domain.model.InsightType
import java.time.Instant

interface InsightHistoryStore {
    suspend fun lastShownAt(): Instant?

    suspend fun wasShown(
        type: InsightType,
        month: String,
    ): Boolean
}
