package com.budgetpilot.feature.history.presentation.editor

import androidx.compose.runtime.Stable
import com.budgetpilot.core.domain.model.Category
import com.budgetpilot.core.domain.money.Money
import com.budgetpilot.core.presentation.UiText
import java.time.LocalDate

enum class ExpenseEditorMode {
    ADD,
    EDIT,
}

@Stable
data class ExpenseEditorState(
    val mode: ExpenseEditorMode = ExpenseEditorMode.ADD,
    val amountText: String = "",
    val merchant: String = "",
    val note: String = "",
    val date: LocalDate = LocalDate.now(),
    val categories: List<Category> = emptyList(),
    val selectedCategoryId: Long? = null,
    val isCategoryManuallySelected: Boolean = false,
    val amountError: UiText? = null,
    val merchantError: UiText? = null,
    val dateError: UiText? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isDeleteConfirmVisible: Boolean = false,
    val isDiscardConfirmVisible: Boolean = false,
) {
    val parsedAmount: Money?
        get() = amountText.toMoneyOrNull()

    val displayAmount: Money
        get() = parsedAmount ?: Money.ZERO

    val isValid: Boolean
        get() {
            val amount = parsedAmount
            return amount != null &&
                amount > Money.ZERO &&
                merchant.isNotBlank() &&
                selectedCategoryId != null &&
                !date.isAfter(LocalDate.now())
        }
}

internal fun String.toMoneyOrNull(): Money? =
    takeIf { it.isNotBlank() }
        ?.let { runCatching { Money.fromPesos(it) }.getOrNull() }
