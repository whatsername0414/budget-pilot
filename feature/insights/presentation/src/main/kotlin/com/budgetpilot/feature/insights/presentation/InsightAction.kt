package com.budgetpilot.feature.insights.presentation

sealed interface InsightAction {
    data object OnDismissClick : InsightAction

    data object OnAskMoreClick : InsightAction
}
