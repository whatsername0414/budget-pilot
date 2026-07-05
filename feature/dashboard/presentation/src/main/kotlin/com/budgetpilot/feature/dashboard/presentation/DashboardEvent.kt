package com.budgetpilot.feature.dashboard.presentation

sealed interface DashboardEvent {
    data object NavigateToExpenseList : DashboardEvent

    data object NavigateToBudgets : DashboardEvent

    data object NavigateToAddExpense : DashboardEvent
}
