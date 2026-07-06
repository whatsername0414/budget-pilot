package com.budgetpilot.feature.insights.domain.model

import java.time.Instant

data class Insight(
    val id: Long,
    val type: InsightType,
    val message: String,
    val month: String,
    val createdAt: Instant,
    val dismissedAt: Instant? = null,
)
