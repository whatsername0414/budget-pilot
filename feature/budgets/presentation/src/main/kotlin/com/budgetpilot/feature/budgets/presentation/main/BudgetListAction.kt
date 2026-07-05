package com.budgetpilot.feature.budgets.presentation.main

sealed interface BudgetListAction {
    data object OnPreviousMonthClick : BudgetListAction

    data object OnNextMonthClick : BudgetListAction

    data class OnEditBudgetClick(
        val categoryId: Long,
    ) : BudgetListAction

    data object OnDismissEditor : BudgetListAction

    data object OnRetryClick : BudgetListAction
}
