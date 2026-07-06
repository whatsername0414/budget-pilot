package com.budgetpilot.feature.capture.presentation.confirm

import java.time.LocalDate

sealed interface ConfirmExpenseAction {
    data object OnBackClick : ConfirmExpenseAction

    data object OnRetakeClick : ConfirmExpenseAction

    data object OnRetryExtractionClick : ConfirmExpenseAction

    data object OnEnterManuallyClick : ConfirmExpenseAction

    data object OnThumbnailClick : ConfirmExpenseAction

    data object OnDismissImageViewer : ConfirmExpenseAction

    data class OnMerchantChange(
        val merchant: String,
    ) : ConfirmExpenseAction

    data class OnDateSelect(
        val date: LocalDate,
    ) : ConfirmExpenseAction

    data class OnAmountChange(
        val amountText: String,
    ) : ConfirmExpenseAction

    data class OnCategorySelect(
        val categoryId: Long,
    ) : ConfirmExpenseAction

    data class OnNoteChange(
        val note: String,
    ) : ConfirmExpenseAction

    data object OnLineItemsToggleClick : ConfirmExpenseAction

    data object OnSaveClick : ConfirmExpenseAction
}
