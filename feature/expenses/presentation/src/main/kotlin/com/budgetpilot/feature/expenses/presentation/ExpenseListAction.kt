package com.budgetpilot.feature.expenses.presentation

import com.budgetpilot.feature.expenses.presentation.model.DateRangePreset

sealed interface ExpenseListAction {
    data class OnSearchQueryChange(
        val query: String,
    ) : ExpenseListAction

    data class OnCategoryFilterSelect(
        val categoryId: Long?,
    ) : ExpenseListAction

    data object OnFiltersClick : ExpenseListAction

    data object OnDismissFilterSheet : ExpenseListAction

    data class OnDateRangePresetSelect(
        val preset: DateRangePreset,
    ) : ExpenseListAction

    data class OnExpenseClick(
        val expenseId: Long,
    ) : ExpenseListAction

    data class OnDeleteExpense(
        val expenseId: Long,
    ) : ExpenseListAction

    data object OnUndoDeleteClick : ExpenseListAction

    data object OnAddExpenseClick : ExpenseListAction

    data object OnRetryClick : ExpenseListAction
}
