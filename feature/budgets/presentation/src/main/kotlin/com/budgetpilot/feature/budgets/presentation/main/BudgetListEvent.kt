package com.budgetpilot.feature.budgets.presentation.main

import com.budgetpilot.core.presentation.UiText

sealed interface BudgetListEvent {
    data class ShowSnackbar(
        val message: String,
    ) : BudgetListEvent

    data class ShowError(
        val message: UiText,
    ) : BudgetListEvent
}
