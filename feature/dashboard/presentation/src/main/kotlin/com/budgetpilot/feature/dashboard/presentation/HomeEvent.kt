package com.budgetpilot.feature.dashboard.presentation

sealed interface HomeEvent {
    data object NavigateToExpenseList : HomeEvent

    data object NavigateToBudgets : HomeEvent

    data object NavigateToAddExpense : HomeEvent
}
