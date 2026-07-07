package com.budgetpilot.feature.insights.domain

import com.budgetpilot.feature.insights.domain.model.Insight
import java.time.Instant

interface InsightStore {
    suspend fun save(insight: Insight): Long

    suspend fun getLatestUndismissed(): Insight?

    suspend fun dismiss(
        id: Long,
        dismissedAt: Instant,
    )
}
