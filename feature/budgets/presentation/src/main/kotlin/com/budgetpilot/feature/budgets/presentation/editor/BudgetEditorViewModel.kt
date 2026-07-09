package com.budgetpilot.feature.budgets.presentation.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgetpilot.core.domain.Result
import com.budgetpilot.core.domain.asEmptyResult
import com.budgetpilot.core.domain.model.Budget
import com.budgetpilot.core.domain.money.Money
import com.budgetpilot.core.domain.onFailure
import com.budgetpilot.core.domain.onSuccess
import com.budgetpilot.core.domain.repository.BudgetRepository
import com.budgetpilot.core.domain.repository.CategoryRepository
import com.budgetpilot.core.presentation.UiText
import com.budgetpilot.core.presentation.money.PesoFormatter
import com.budgetpilot.core.presentation.toUiText
import com.budgetpilot.feature.budgets.presentation.budgets.toMonthString
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

private val MonthLabelFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH)

class BudgetEditorViewModel(
    private val categoryId: Long,
    private val month: YearMonth,
    private val budgetRepository: BudgetRepository,
    private val categoryRepository: CategoryRepository,
) : ViewModel() {
    private var existingBudget: Budget? = null

    private val _state =
        MutableStateFlow(
            BudgetEditorState(categoryId = categoryId, monthLabel = month.format(MonthLabelFormatter)),
        )
    val state = _state.asStateFlow()

    private val _events = Channel<BudgetEditorEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        load()
    }

    fun onAction(action: BudgetEditorAction) {
        when (action) {
            is BudgetEditorAction.OnAmountTextChange -> onAmountTextChange(action.text)
            is BudgetEditorAction.OnQuickAmountSelect ->
                _state.update { it.copy(amountText = action.amount.toEditableText(), amountError = null) }
            BudgetEditorAction.OnSaveClick -> save()
            BudgetEditorAction.OnRemoveClick -> _state.update { it.copy(isRemoveConfirmVisible = true) }
            BudgetEditorAction.OnConfirmRemoveClick -> remove()
            BudgetEditorAction.OnDismissRemoveDialog -> _state.update { it.copy(isRemoveConfirmVisible = false) }
            BudgetEditorAction.OnDismissClick ->
                viewModelScope.launch { _events.send(BudgetEditorEvent.Dismiss()) }
        }
    }

    private fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            val categoryResult = categoryRepository.getCategoryById(categoryId)
            if (categoryResult is Result.Error) {
                _state.update { it.copy(isLoading = false) }
                _events.send(BudgetEditorEvent.ShowError(categoryResult.error.toUiText()))
                return@launch
            }
            val category = (categoryResult as Result.Success).data

            val budgetResult = budgetRepository.getBudget(categoryId, month.toMonthString())
            val budget = (budgetResult as? Result.Success)?.data
            existingBudget = budget

            val lastMonthResult =
                budgetRepository.spentForCategoryInMonth(categoryId, month.minusMonths(1).toMonthString())
            val lastMonthSpent = (lastMonthResult as? Result.Success)?.data

            _state.update {
                it.copy(
                    categoryName = category.name,
                    categoryIconKey = category.iconKey,
                    categoryColorKey = category.colorKey,
                    amountText = budget?.amount?.toEditableText().orEmpty(),
                    isEditing = budget != null,
                    lastMonthSpent = lastMonthSpent,
                    isLoading = false,
                )
            }
        }
    }

    private fun onAmountTextChange(text: String) {
        _state.update { it.copy(amountText = text.sanitizeAmountInput(), amountError = null) }
    }

    private fun save() {
        val current = _state.value
        val amount = current.parsedAmount
        if (amount == null || amount <= Money.ZERO) {
            _state.update { it.copy(amountError = UiText.DynamicString(AMOUNT_REQUIRED_MESSAGE)) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }

            val existing = existingBudget
            val result =
                if (existing != null) {
                    budgetRepository.updateBudget(existing.copy(amount = amount))
                } else {
                    budgetRepository
                        .addBudget(
                            Budget(id = 0, categoryId = categoryId, month = month.toMonthString(), amount = amount),
                        ).asEmptyResult()
                }

            result
                .onSuccess {
                    _state.update { it.copy(isSaving = false) }
                    val message = "${current.categoryName} budget set to ${PesoFormatter.format(amount)}"
                    _events.send(BudgetEditorEvent.Dismiss(confirmationMessage = message))
                }.onFailure { error ->
                    _state.update { it.copy(isSaving = false) }
                    _events.send(BudgetEditorEvent.ShowError(error.toUiText()))
                }
        }
    }

    private fun remove() {
        val existing = existingBudget ?: return
        _state.update { it.copy(isRemoveConfirmVisible = false) }
        viewModelScope.launch {
            budgetRepository
                .deleteBudget(existing)
                .onSuccess {
                    _events.send(BudgetEditorEvent.Dismiss(confirmationMessage = "Budget removed"))
                }.onFailure { error ->
                    _events.send(BudgetEditorEvent.ShowError(error.toUiText()))
                }
        }
    }

    companion object {
        private const val AMOUNT_REQUIRED_MESSAGE = "Enter a budget amount"
    }
}

private fun Money.toEditableText(): String {
    val whole = centavos / 100
    val fraction = (centavos % 100).let { if (it < 0) -it else it }
    return "$whole.${fraction.toString().padStart(2, '0')}"
}

private fun String.sanitizeAmountInput(): String {
    val digitsAndDot = filter { it.isDigit() || it == '.' }
    val firstDotIndex = digitsAndDot.indexOf('.')
    if (firstDotIndex == -1) return digitsAndDot
    val wholePart = digitsAndDot.substring(0, firstDotIndex)
    val fractionPart = digitsAndDot.substring(firstDotIndex + 1).replace(".", "").take(2)
    return "$wholePart.$fractionPart"
}
