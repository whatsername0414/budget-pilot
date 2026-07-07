package com.budgetpilot.feature.insights.presentation

import androidx.compose.runtime.Stable

@Stable
data class InsightCardUi(
    val message: String,
)

data class InsightState(
    val card: InsightCardUi? = null,
)
