package com.budgetpilot.feature.home.presentation

sealed interface HomeAction {
    data object OnSeeAllExpensesClick : HomeAction

    data object OnSeeBudgetsClick : HomeAction

    data object OnAddExpenseClick : HomeAction

    data object OnRetryClick : HomeAction

    data object OnSettingsClick : HomeAction
}
