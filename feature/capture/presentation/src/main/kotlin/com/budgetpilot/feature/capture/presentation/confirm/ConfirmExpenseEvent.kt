package com.budgetpilot.feature.capture.presentation.confirm

import com.budgetpilot.core.presentation.UiText

sealed interface ConfirmExpenseEvent {
    data object NavigateBack : ConfirmExpenseEvent

    data object NavigateToRetake : ConfirmExpenseEvent

    data class NavigateHome(
        val confirmationMessage: String,
    ) : ConfirmExpenseEvent

    data class ShowError(
        val message: UiText,
    ) : ConfirmExpenseEvent
}
