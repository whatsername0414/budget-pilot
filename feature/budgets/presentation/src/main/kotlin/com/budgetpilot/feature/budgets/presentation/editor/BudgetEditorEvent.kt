package com.budgetpilot.feature.budgets.presentation.editor

import com.budgetpilot.core.presentation.UiText

sealed interface BudgetEditorEvent {
    data class Dismiss(
        val confirmationMessage: String? = null,
    ) : BudgetEditorEvent

    data class ShowError(
        val message: UiText,
    ) : BudgetEditorEvent
}
