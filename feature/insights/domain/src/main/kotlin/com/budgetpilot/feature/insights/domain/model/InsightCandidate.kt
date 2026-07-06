package com.budgetpilot.feature.insights.domain.model

data class InsightCandidate(
    val type: InsightType,
    val priority: Int,
    val data: InsightData,
)
