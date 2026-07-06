package com.budgetpilot.feature.insights.data

import com.budgetpilot.feature.insights.domain.model.Insight

sealed interface InsightCheckResult {
    data class Stored(
        val insight: Insight,
    ) : InsightCheckResult

    data object Throttled : InsightCheckResult

    data object NoCandidate : InsightCheckResult
}
