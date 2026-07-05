package com.budgetpilot.feature.expenses.presentation.editor

import com.budgetpilot.core.presentation.UiText

sealed interface ExpenseEditorEvent {
    data class NavigateBack(
        val confirmationMessage: String? = null,
    ) : ExpenseEditorEvent

    data class ShowError(
        val message: UiText,
    ) : ExpenseEditorEvent
}
