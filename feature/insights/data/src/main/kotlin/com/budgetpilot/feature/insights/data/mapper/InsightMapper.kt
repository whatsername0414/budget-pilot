package com.budgetpilot.feature.insights.data.mapper

import com.budgetpilot.core.database.entity.InsightEntity
import com.budgetpilot.feature.insights.domain.model.Insight
import com.budgetpilot.feature.insights.domain.model.InsightType

fun InsightEntity.toInsight(): Insight =
    Insight(
        id = id,
        type = InsightType.valueOf(type),
        message = message,
        month = month,
        createdAt = createdAt,
        dismissedAt = dismissedAt,
        followUpQuestion = followUpQuestion,
    )

fun Insight.toEntity(): InsightEntity =
    InsightEntity(
        id = id,
        type = type.name,
        message = message,
        month = month,
        createdAt = createdAt,
        dismissedAt = dismissedAt,
        followUpQuestion = followUpQuestion,
    )
