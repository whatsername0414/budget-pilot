package com.budgetpilot.feature.insights.presentation

sealed interface InsightEvent {
    data class NavigateToAsk(
        val prefillQuestion: String,
    ) : InsightEvent
}
