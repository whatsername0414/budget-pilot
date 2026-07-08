package com.budgetpilot.feature.capture.presentation.confirm

import androidx.compose.runtime.Stable
import com.budgetpilot.core.domain.model.Category
import com.budgetpilot.core.domain.money.Money
import com.budgetpilot.core.presentation.UiText
import com.budgetpilot.feature.capture.domain.model.Confidence
import com.budgetpilot.feature.capture.domain.model.LineItem
import com.budgetpilot.feature.capture.domain.model.ReceiptType
import com.budgetpilot.feature.capture.presentation.R
import java.time.LocalDate

enum class ConfirmExpensePhase {
    LOADING,
    LOADED,
    ERROR,
}

@Stable
data class ConfirmExpenseState(
    val phase: ConfirmExpensePhase = ConfirmExpensePhase.LOADING,
    val stagedStatusText: UiText = UiText.StringResource(R.string.confirm_status_uploading),
    val errorMessage: UiText? = null,
    val canUseOfflineScan: Boolean = false,
    val imagePath: String = "",
    val receiptType: ReceiptType = ReceiptType.PAPER,
    val merchant: String = "",
    val merchantConfidence: Confidence = Confidence.HIGH,
    val date: LocalDate = LocalDate.now(),
    val dateConfidence: Confidence = Confidence.HIGH,
    val amountText: String = "",
    val amountConfidence: Confidence = Confidence.HIGH,
    val amountError: UiText? = null,
    val lineItems: List<LineItem> = emptyList(),
    val lineItemsConfidence: Confidence = Confidence.HIGH,
    val isLineItemsExpanded: Boolean = false,
    val isLineItemSheetVisible: Boolean = false,
    val editingLineItemIndex: Int? = null,
    val lineItemDraftDescription: String = "",
    val lineItemDraftPriceText: String = "",
    val categories: List<Category> = emptyList(),
    val suggestedCategoryName: String? = null,
    val selectedCategoryId: Long? = null,
    val isCategoryManuallySelected: Boolean = false,
    val categoryConfidence: Confidence = Confidence.HIGH,
    val isSaving: Boolean = false,
    val isImageViewerVisible: Boolean = false,
) {
    val isGCashOrMaya: Boolean
        get() = receiptType == ReceiptType.GCASH || receiptType == ReceiptType.MAYA

    val parsedAmount: Money?
        get() = amountText.toMoneyOrNull()

    val displayAmount: Money
        get() = parsedAmount ?: Money.ZERO

    val parsedLineItemDraftPrice: Money?
        get() = lineItemDraftPriceText.toMoneyOrNull()

    val isLineItemDraftValid: Boolean
        get() {
            val price = parsedLineItemDraftPrice
            return lineItemDraftDescription.isNotBlank() && price != null && price > Money.ZERO
        }

    val isValid: Boolean
        get() {
            val amount = parsedAmount
            return amount != null &&
                amount > Money.ZERO &&
                merchant.isNotBlank() &&
                selectedCategoryId != null
        }
}

internal fun String.toMoneyOrNull(): Money? =
    takeIf { it.isNotBlank() }
        ?.let { runCatching { Money.fromPesos(it) }.getOrNull() }
