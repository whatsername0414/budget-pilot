package com.budgetpilot.feature.expenses.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgetpilot.core.domain.map
import com.budgetpilot.core.domain.merchant.PhMerchantCatalog
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
import com.budgetpilot.feature.expenses.presentation.components.AmountKeypadKeys
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate

class ExpenseEditorViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository,
) : ViewModel() {
    private val expenseId: Long? = savedStateHandle.get<Long>(KEY_EXPENSE_ID)
    private val hasRestoredFormState: Boolean =
        savedStateHandle.contains(KEY_AMOUNT) ||
            savedStateHandle.contains(KEY_MERCHANT) ||
            savedStateHandle.contains(KEY_NOTE)
    private var loadedExpense: Expense? = null
    private var initialSnapshot = FormSnapshot()

    private val _state =
        MutableStateFlow(
            ExpenseEditorState(
                mode = if (expenseId != null) ExpenseEditorMode.EDIT else ExpenseEditorMode.ADD,
                amountText = savedStateHandle[KEY_AMOUNT] ?: "",
                merchant = savedStateHandle[KEY_MERCHANT] ?: "",
                note = savedStateHandle[KEY_NOTE] ?: "",
            ),
        )
    val state = _state.asStateFlow()

    private val _events = Channel<ExpenseEditorEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        initialSnapshot = _state.value.toSnapshot()
        observeCategories()
        expenseId?.let(::loadExpense)
    }

    fun onAction(action: ExpenseEditorAction) {
        when (action) {
            is ExpenseEditorAction.OnAmountKeyPress -> onAmountKeyPress(action.key)
            is ExpenseEditorAction.OnMerchantChange -> onMerchantChange(action.merchant)
            ExpenseEditorAction.OnMerchantFieldBlur -> {
                if (_state.value.merchant.isBlank()) {
                    _state.update { it.copy(merchantError = UiText.DynamicString(MERCHANT_REQUIRED_MESSAGE)) }
                }
            }
            is ExpenseEditorAction.OnCategorySelect -> {
                _state.update { it.copy(selectedCategoryId = action.categoryId, isCategoryManuallySelected = true) }
            }
            is ExpenseEditorAction.OnDateSelect -> {
                val error =
                    if (action.date.isAfter(LocalDate.now())) UiText.DynamicString(FUTURE_DATE_MESSAGE) else null
                _state.update { it.copy(date = action.date, dateError = error) }
            }
            is ExpenseEditorAction.OnNoteChange -> {
                _state.update { it.copy(note = action.note) }
                savedStateHandle[KEY_NOTE] = action.note
            }
            ExpenseEditorAction.OnSaveClick -> save()
            ExpenseEditorAction.OnDismissClick -> onDismissClick()
            ExpenseEditorAction.OnConfirmDiscardClick -> navigateBack()
            ExpenseEditorAction.OnDismissDiscardDialog -> _state.update { it.copy(isDiscardConfirmVisible = false) }
            ExpenseEditorAction.OnDeleteClick -> _state.update { it.copy(isDeleteConfirmVisible = true) }
            ExpenseEditorAction.OnConfirmDeleteClick -> deleteExpense()
            ExpenseEditorAction.OnDismissDeleteDialog -> _state.update { it.copy(isDeleteConfirmVisible = false) }
        }
    }

    private fun observeCategories() {
        viewModelScope.launch {
            categoryRepository.observeCategories().collect { categories ->
                _state.update { it.copy(categories = categories) }
            }
        }
    }

    private fun loadExpense(id: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            expenseRepository
                .getExpenseById(id)
                .onSuccess { expense ->
                    loadedExpense = expense
                    _state.update { current ->
                        current.copy(
                            isLoading = false,
                            amountText = if (hasRestoredFormState) current.amountText else expense.amount.toEditableText(),
                            merchant = if (hasRestoredFormState) current.merchant else expense.merchant,
                            note = if (hasRestoredFormState) current.note else expense.note.orEmpty(),
                            date = expense.date,
                            selectedCategoryId = expense.categoryId,
                            isCategoryManuallySelected = true,
                        )
                    }
                    initialSnapshot = _state.value.toSnapshot()
                }.onFailure { error ->
                    _state.update { it.copy(isLoading = false) }
                    _events.send(ExpenseEditorEvent.ShowError(error.toUiText()))
                }
        }
    }

    private fun onAmountKeyPress(key: String) {
        val current = _state.value.amountText
        val updated =
            when (key) {
                AmountKeypadKeys.BACKSPACE -> current.dropLast(1)
                "." -> if (current.contains(".")) current else current.ifEmpty { "0" } + "."
                else -> {
                    val fractionDigits = current.substringAfter(".", missingDelimiterValue = "")
                    if (current.contains(".") && fractionDigits.length >= 2) current else current + key
                }
            }
        _state.update { it.copy(amountText = updated, amountError = null) }
        savedStateHandle[KEY_AMOUNT] = updated
    }

    private fun onMerchantChange(merchant: String) {
        _state.update { it.copy(merchant = merchant, merchantError = null) }
        savedStateHandle[KEY_MERCHANT] = merchant

        if (_state.value.isCategoryManuallySelected) return
        val suggested =
            PhMerchantCatalog
                .suggestCategory(merchant)
                ?.let { name -> _state.value.categories.find { it.name.equals(name, ignoreCase = true) } }
        if (suggested != null) {
            _state.update { it.copy(selectedCategoryId = suggested.id) }
        }
    }

    private fun onDismissClick() {
        if (_state.value.toSnapshot() != initialSnapshot) {
            _state.update { it.copy(isDiscardConfirmVisible = true) }
        } else {
            navigateBack()
        }
    }

    private fun navigateBack() {
        viewModelScope.launch { _events.send(ExpenseEditorEvent.NavigateBack()) }
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

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }

            val result =
                when (current.mode) {
                    ExpenseEditorMode.ADD ->
                        expenseRepository
                            .addExpense(
                                Expense(
                                    id = 0,
                                    amount = amount,
                                    merchant = current.merchant.trim(),
                                    categoryId = categoryId,
                                    date = current.date,
                                    note = current.note.trim().ifBlank { null },
                                    source = ExpenseSource.MANUAL,
                                    imageUri = null,
                                    createdAt = Instant.now(),
                                ),
                            ).map { }
                    ExpenseEditorMode.EDIT -> {
                        val existing = loadedExpense
                        if (existing == null) {
                            _state.update { it.copy(isSaving = false) }
                            return@launch
                        }
                        expenseRepository.updateExpense(
                            existing.copy(
                                amount = amount,
                                merchant = current.merchant.trim(),
                                categoryId = categoryId,
                                date = current.date,
                                note = current.note.trim().ifBlank { null },
                            ),
                        )
                    }
                }

            result
                .onSuccess {
                    _state.update { it.copy(isSaving = false) }
                    val verb = if (current.mode == ExpenseEditorMode.ADD) "Saved" else "Updated"
                    val message = "$verb ${PesoFormatter.format(amount)} to $categoryName"
                    _events.send(ExpenseEditorEvent.NavigateBack(confirmationMessage = message))
                }.onFailure { error ->
                    _state.update { it.copy(isSaving = false) }
                    _events.send(ExpenseEditorEvent.ShowError(error.toUiText()))
                }
        }
    }

    private fun deleteExpense() {
        val existing = loadedExpense ?: return
        _state.update { it.copy(isDeleteConfirmVisible = false) }
        viewModelScope.launch {
            expenseRepository
                .deleteExpense(existing)
                .onSuccess {
                    _events.send(ExpenseEditorEvent.NavigateBack(confirmationMessage = "Expense deleted"))
                }.onFailure { error ->
                    _events.send(ExpenseEditorEvent.ShowError(error.toUiText()))
                }
        }
    }

    private data class FormSnapshot(
        val amountText: String = "",
        val merchant: String = "",
        val note: String = "",
        val date: LocalDate = LocalDate.now(),
        val selectedCategoryId: Long? = null,
    )

    private fun ExpenseEditorState.toSnapshot() =
        FormSnapshot(
            amountText = amountText,
            merchant = merchant,
            note = note,
            date = date,
            selectedCategoryId = selectedCategoryId,
        )

    companion object {
        const val KEY_EXPENSE_ID = "expenseId"
        const val KEY_AMOUNT = "amountText"
        const val KEY_MERCHANT = "merchant"
        const val KEY_NOTE = "note"
        private const val MERCHANT_REQUIRED_MESSAGE = "Enter who you paid"
        private const val FUTURE_DATE_MESSAGE = "Date can't be in the future"
    }
}

private fun Money.toEditableText(): String {
    val whole = centavos / 100
    val fraction = (centavos % 100).let { if (it < 0) -it else it }
    return "$whole.${fraction.toString().padStart(2, '0')}"
}
