package com.budgetpilot.feature.history.presentation.navigation

import kotlinx.serialization.Serializable

@Serializable
data object HistoryRoute

@Serializable
data class ExpenseEditorRoute(
    val expenseId: Long? = null,
)
