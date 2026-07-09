package com.budgetpilot.feature.history.presentation.expenses

import com.budgetpilot.core.presentation.UiText

sealed interface ExpenseListEvent {
    data class ShowError(
        val message: UiText,
    ) : ExpenseListEvent

    data class NavigateToExpenseEditor(
        val expenseId: Long?,
    ) : ExpenseListEvent
}
