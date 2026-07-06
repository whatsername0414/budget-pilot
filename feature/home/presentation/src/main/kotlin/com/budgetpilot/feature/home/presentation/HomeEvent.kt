package com.budgetpilot.feature.home.presentation

sealed interface HomeEvent {
    data object NavigateToExpenseList : HomeEvent

    data object NavigateToBudgets : HomeEvent

    data object NavigateToAddExpense : HomeEvent

    data object NavigateToSettings : HomeEvent
}
