package com.budgetpilot.feature.insights.domain

import com.budgetpilot.feature.insights.domain.model.InsightType
import java.time.Clock
import java.time.Duration

private const val COOLDOWN_HOURS = 48L

class InsightThrottlePolicy(
    private val historyStore: InsightHistoryStore,
    private val clock: Clock = Clock.systemDefaultZone(),
) {
    suspend fun canShow(
        type: InsightType,
        month: String,
    ): Boolean {
        val lastShownAt = historyStore.lastShownAt()
        val cooledDown = lastShownAt == null || Duration.between(lastShownAt, clock.instant()) >= MIN_INTERVAL
        if (!cooledDown) return false

        return !historyStore.wasShown(type, month)
    }

    companion object {
        val MIN_INTERVAL: Duration = Duration.ofHours(COOLDOWN_HOURS)
    }
}
