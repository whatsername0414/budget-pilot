package com.budgetpilot.feature.budgets.presentation.editor

import androidx.compose.runtime.Stable
import com.budgetpilot.core.domain.money.Money
import com.budgetpilot.core.presentation.UiText

@Stable
data class BudgetEditorState(
    val categoryId: Long = 0,
    val categoryName: String = "",
    val categoryIconKey: String = "",
    val categoryColorKey: String = "",
    val monthLabel: String = "",
    val amountText: String = "",
    val lastMonthSpent: Money? = null,
    val isEditing: Boolean = false,
    val amountError: UiText? = null,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isRemoveConfirmVisible: Boolean = false,
) {
    val parsedAmount: Money?
        get() = amountText.toMoneyOrNull()

    val displayAmount: Money
        get() = parsedAmount ?: Money.ZERO
}

private fun String.toMoneyOrNull(): Money? =
    takeIf { it.isNotBlank() }
        ?.let { runCatching { Money.fromPesos(it) }.getOrNull() }
