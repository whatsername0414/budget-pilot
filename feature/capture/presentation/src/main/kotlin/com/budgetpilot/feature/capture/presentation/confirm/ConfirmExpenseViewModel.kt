package com.budgetpilot.feature.capture.presentation.confirm

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgetpilot.core.domain.model.Expense
import com.budgetpilot.core.domain.model.ExpenseSource
import com.budgetpilot.core.domain.money.Money
import com.budgetpilot.core.domain.onFailure
import com.budgetpilot.core.domain.onSuccess
import com.budgetpilot.core.domain.repository.CategoryRepository
import com.budgetpilot.core.domain.repository.ExpenseRepository
import com.budgetpilot.core.presentation.UiText
import com.budgetpilot.core.presentation.money.PesoFormatter
import com.budgetpilot.core.presentation.toUiText
import com.budgetpilot.feature.capture.domain.ReceiptExtractor
import com.budgetpilot.feature.capture.domain.ReceiptImage
import com.budgetpilot.feature.capture.domain.model.Confidence
import com.budgetpilot.feature.capture.domain.model.ExtractedReceipt
import com.budgetpilot.feature.capture.domain.model.ReceiptType
import com.budgetpilot.feature.capture.presentation.R
import com.budgetpilot.feature.capture.presentation.toUiText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.time.Instant
import java.time.LocalDate

class ConfirmExpenseViewModel(
    savedStateHandle: SavedStateHandle,
    private val receiptExtractor: ReceiptExtractor,
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository,
) : ViewModel() {
    private val imagePath: String = savedStateHandle.get<String>(KEY_IMAGE_PATH).orEmpty()

    private val _state = MutableStateFlow(ConfirmExpenseState(imagePath = imagePath))
    val state = _state.asStateFlow()

    private val _events = Channel<ConfirmExpenseEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        observeCategories()
        extract()
    }

    fun onAction(action: ConfirmExpenseAction) {
        when (action) {
            ConfirmExpenseAction.OnBackClick -> sendEvent(ConfirmExpenseEvent.NavigateBack)
            ConfirmExpenseAction.OnRetakeClick -> sendEvent(ConfirmExpenseEvent.NavigateToRetake)
            ConfirmExpenseAction.OnRetryExtractionClick -> extract()
            ConfirmExpenseAction.OnEnterManuallyClick -> enterManually()
            ConfirmExpenseAction.OnThumbnailClick -> _state.update { it.copy(isImageViewerVisible = true) }
            ConfirmExpenseAction.OnDismissImageViewer -> _state.update { it.copy(isImageViewerVisible = false) }
            is ConfirmExpenseAction.OnMerchantChange ->
                _state.update { it.copy(merchant = action.merchant, merchantConfidence = Confidence.HIGH) }
            is ConfirmExpenseAction.OnDateSelect ->
                _state.update { it.copy(date = action.date, dateConfidence = Confidence.HIGH) }
            is ConfirmExpenseAction.OnAmountChange ->
                _state.update {
                    it.copy(amountText = action.amountText, amountConfidence = Confidence.HIGH, amountError = null)
                }
            is ConfirmExpenseAction.OnCategorySelect ->
                _state.update {
                    it.copy(
                        selectedCategoryId = action.categoryId,
                        isCategoryManuallySelected = true,
                        categoryConfidence = Confidence.HIGH,
                    )
                }
            ConfirmExpenseAction.OnLineItemsToggleClick ->
                _state.update { it.copy(isLineItemsExpanded = !it.isLineItemsExpanded) }
            ConfirmExpenseAction.OnSaveClick -> save()
        }
    }

    private fun observeCategories() {
        viewModelScope.launch {
            categoryRepository.observeCategories().collect { categories ->
                _state.update { it.copy(categories = categories) }
                tryResolveSuggestedCategory()
            }
        }
    }

    private fun extract() {
        _state.update {
            it.copy(
                phase = ConfirmExpensePhase.LOADING,
                errorMessage = null,
                stagedStatusText = UiText.StringResource(R.string.confirm_status_uploading),
            )
        }
        cycleStagedStatus()
        viewModelScope.launch {
            val image =
                ReceiptImage(path = imagePath) {
                    withContext(Dispatchers.IO) { File(imagePath).readBytes() }
                }
            receiptExtractor
                .extract(image)
                .onSuccess(::applyExtractedReceipt)
                .onFailure { error ->
                    _state.update { it.copy(phase = ConfirmExpensePhase.ERROR, errorMessage = error.toUiText()) }
                }
        }
    }

    private fun cycleStagedStatus() {
        viewModelScope.launch {
            val stages =
                listOf(
                    R.string.confirm_status_uploading,
                    R.string.confirm_status_reading,
                    R.string.confirm_status_almost_done,
                )
            for (stageRes in stages) {
                if (_state.value.phase != ConfirmExpensePhase.LOADING) return@launch
                _state.update { it.copy(stagedStatusText = UiText.StringResource(stageRes)) }
                delay(STAGED_STATUS_DELAY_MS)
            }
        }
    }

    private fun applyExtractedReceipt(receipt: ExtractedReceipt) {
        _state.update {
            it.copy(
                phase = ConfirmExpensePhase.LOADED,
                receiptType = receipt.receiptType.value,
                merchant = receipt.merchant.value,
                merchantConfidence = receipt.merchant.confidence,
                date = receipt.date.value,
                dateConfidence = receipt.date.confidence,
                amountText = receipt.total.value.toEditableText(),
                amountConfidence = receipt.total.confidence,
                lineItems = receipt.lineItems.value,
                lineItemsConfidence = receipt.lineItems.confidence,
                suggestedCategoryName = receipt.suggestedCategory.value,
                categoryConfidence = receipt.suggestedCategory.confidence,
            )
        }
        tryResolveSuggestedCategory()
    }

    private fun tryResolveSuggestedCategory() {
        val current = _state.value
        val alreadyResolved = current.isCategoryManuallySelected || current.selectedCategoryId != null
        val match =
            current.suggestedCategoryName
                ?.takeUnless { alreadyResolved }
                ?.let { name -> current.categories.find { it.name.equals(name, ignoreCase = true) } }
        if (match != null) {
            _state.update { it.copy(selectedCategoryId = match.id) }
        }
    }

    private fun enterManually() {
        _state.update {
            it.copy(
                phase = ConfirmExpensePhase.LOADED,
                errorMessage = null,
                merchant = "",
                merchantConfidence = Confidence.HIGH,
                date = LocalDate.now(),
                dateConfidence = Confidence.HIGH,
                amountText = "",
                amountConfidence = Confidence.HIGH,
                lineItems = emptyList(),
                lineItemsConfidence = Confidence.HIGH,
                suggestedCategoryName = null,
                selectedCategoryId = null,
                isCategoryManuallySelected = false,
                categoryConfidence = Confidence.HIGH,
            )
        }
    }

    private fun save() {
        val current = _state.value
        val amount = current.parsedAmount
        val categoryId = current.selectedCategoryId
        if (!current.isValid || amount == null || categoryId == null) return

        val categoryName =
            current.categories
                .find { it.id == categoryId }
                ?.name
                .orEmpty()
        val source =
            when (current.receiptType) {
                ReceiptType.PAPER -> ExpenseSource.RECEIPT
                ReceiptType.GCASH -> ExpenseSource.GCASH
                ReceiptType.MAYA -> ExpenseSource.MAYA
            }

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            expenseRepository
                .addExpense(
                    Expense(
                        id = 0,
                        amount = amount,
                        merchant = current.merchant.trim(),
                        categoryId = categoryId,
                        date = current.date,
                        note = null,
                        source = source,
                        imageUri = current.imagePath,
                        createdAt = Instant.now(),
                    ),
                ).onSuccess {
                    _state.update { it.copy(isSaving = false) }
                    val message = "Saved ${PesoFormatter.format(amount)} to $categoryName"
                    _events.send(ConfirmExpenseEvent.NavigateHome(confirmationMessage = message))
                }.onFailure { error ->
                    _state.update { it.copy(isSaving = false) }
                    _events.send(ConfirmExpenseEvent.ShowError(error.toUiText()))
                }
        }
    }

    private fun sendEvent(event: ConfirmExpenseEvent) {
        viewModelScope.launch { _events.send(event) }
    }

    companion object {
        const val KEY_IMAGE_PATH = "imagePath"
        private const val STAGED_STATUS_DELAY_MS = 450L
    }
}

private fun Money.toEditableText(): String {
    val whole = centavos / 100
    val fraction = (centavos % 100).let { if (it < 0) -it else it }
    return "$whole.${fraction.toString().padStart(2, '0')}"
}
