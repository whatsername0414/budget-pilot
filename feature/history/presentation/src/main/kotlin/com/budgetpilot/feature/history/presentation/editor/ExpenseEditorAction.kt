package com.budgetpilot.feature.history.presentation.editor

import java.time.LocalDate

sealed interface ExpenseEditorAction {
    data class OnAmountKeyPress(
        val key: String,
    ) : ExpenseEditorAction

    data class OnMerchantChange(
        val merchant: String,
    ) : ExpenseEditorAction

    data object OnMerchantFieldBlur : ExpenseEditorAction

    data class OnCategorySelect(
        val categoryId: Long,
    ) : ExpenseEditorAction

    data class OnDateSelect(
        val date: LocalDate,
    ) : ExpenseEditorAction

    data class OnNoteChange(
        val note: String,
    ) : ExpenseEditorAction

    data object OnSaveClick : ExpenseEditorAction

    data object OnDismissClick : ExpenseEditorAction

    data object OnConfirmDiscardClick : ExpenseEditorAction

    data object OnDismissDiscardDialog : ExpenseEditorAction

    data object OnDeleteClick : ExpenseEditorAction

    data object OnConfirmDeleteClick : ExpenseEditorAction

    data object OnDismissDeleteDialog : ExpenseEditorAction
}
