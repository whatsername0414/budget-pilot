package com.budgetpilot.feature.budgets.presentation.editor

import com.budgetpilot.core.domain.money.Money

sealed interface BudgetEditorAction {
    data class OnAmountTextChange(
        val text: String,
    ) : BudgetEditorAction

    data class OnQuickAmountSelect(
        val amount: Money,
    ) : BudgetEditorAction

    data object OnSaveClick : BudgetEditorAction

    data object OnRemoveClick : BudgetEditorAction

    data object OnConfirmRemoveClick : BudgetEditorAction

    data object OnDismissRemoveDialog : BudgetEditorAction

    data object OnDismissClick : BudgetEditorAction
}
