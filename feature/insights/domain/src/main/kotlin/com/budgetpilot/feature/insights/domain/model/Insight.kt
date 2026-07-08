package com.budgetpilot.feature.insights.domain.model

import java.time.Instant

data class Insight(
    val id: Long,
    val type: InsightType,
    val message: String,
    val month: String,
    val createdAt: Instant,
    val dismissedAt: Instant? = null,
    // Null only for insights persisted before this field existed; presentation falls back to a
    // generic per-type question for those.
    val followUpQuestion: String? = null,
)
