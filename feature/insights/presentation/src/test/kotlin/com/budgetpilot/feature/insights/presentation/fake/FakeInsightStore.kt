package com.budgetpilot.feature.insights.presentation.fake

import com.budgetpilot.feature.insights.domain.InsightHistoryStore
import com.budgetpilot.feature.insights.domain.InsightStore
import com.budgetpilot.feature.insights.domain.model.Insight
import com.budgetpilot.feature.insights.domain.model.InsightType
import java.time.Instant

class FakeInsightStore(
    latestUndismissed: Insight? = null,
) : InsightStore,
    InsightHistoryStore {
    var latestUndismissed: Insight? = latestUndismissed
        private set
    val dismissed = mutableListOf<Pair<Long, Instant>>()

    override suspend fun save(insight: Insight): Long = insight.id

    override suspend fun getLatestUndismissed(): Insight? = latestUndismissed

    override suspend fun dismiss(
        id: Long,
        dismissedAt: Instant,
    ) {
        dismissed += id to dismissedAt
        if (latestUndismissed?.id == id) latestUndismissed = null
    }

    override suspend fun lastShownAt(): Instant? = null

    override suspend fun wasShown(
        type: InsightType,
        month: String,
    ): Boolean = false
}
