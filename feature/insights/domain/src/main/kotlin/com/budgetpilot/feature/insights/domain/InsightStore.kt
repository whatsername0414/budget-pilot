package com.budgetpilot.feature.insights.domain

import com.budgetpilot.feature.insights.domain.model.Insight

interface InsightStore {
    suspend fun save(insight: Insight): Long
}
