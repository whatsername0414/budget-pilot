package com.budgetpilot.feature.dashboard.presentation

sealed interface DashboardAction {
    data object OnSeeAllExpensesClick : DashboardAction

    data object OnSeeBudgetsClick : DashboardAction

    data object OnAddExpenseClick : DashboardAction

    data object OnRetryClick : DashboardAction
}
