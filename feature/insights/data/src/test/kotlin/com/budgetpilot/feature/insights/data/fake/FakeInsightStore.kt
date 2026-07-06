package com.budgetpilot.feature.insights.data.fake

import com.budgetpilot.feature.insights.domain.InsightHistoryStore
import com.budgetpilot.feature.insights.domain.InsightStore
import com.budgetpilot.feature.insights.domain.model.Insight
import com.budgetpilot.feature.insights.domain.model.InsightType
import java.time.Instant

class FakeInsightStore(
    private val lastShownAt: Instant? = null,
    private val shownTypesByMonth: Set<Pair<InsightType, String>> = emptySet(),
) : InsightStore,
    InsightHistoryStore {
    val saved = mutableListOf<Insight>()
    private var nextId = 1L

    override suspend fun save(insight: Insight): Long {
        val id = nextId++
        saved += insight.copy(id = id)
        return id
    }

    override suspend fun lastShownAt(): Instant? = lastShownAt

    override suspend fun wasShown(
        type: InsightType,
        month: String,
    ): Boolean = (type to month) in shownTypesByMonth
}
