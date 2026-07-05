package com.budgetpilot.feature.budgets.presentation.charts

sealed interface ChartsAction {
    data object OnPreviousMonthClick : ChartsAction

    data object OnNextMonthClick : ChartsAction

    data object OnRetryClick : ChartsAction
}
