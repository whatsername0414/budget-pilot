package com.budgetpilot.feature.expenses.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgetpilot.core.domain.DataError
import com.budgetpilot.core.domain.model.Expense
import com.budgetpilot.core.domain.model.ExpenseFilter
import com.budgetpilot.core.domain.onFailure
import com.budgetpilot.core.domain.onSuccess
import com.budgetpilot.core.domain.repository.CategoryRepository
import com.budgetpilot.core.domain.repository.ExpenseRepository
import com.budgetpilot.core.presentation.toUiText
import com.budgetpilot.feature.expenses.presentation.main.model.DateRangePreset
import com.budgetpilot.feature.expenses.presentation.main.model.toDayGroups
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ExpenseListViewModel(
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(ExpenseListState())
    val state = _state.asStateFlow()

    private val _events = Channel<ExpenseListEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private val filterParams = MutableStateFlow(FilterParams())

    private var expensesById: Map<Long, Expense> = emptyMap()
    private var pendingUndoExpense: Expense? = null

    init {
        observeExpenses()
    }

    fun onAction(action: ExpenseListAction) {
        when (action) {
            is ExpenseListAction.OnSearchQueryChange -> onSearchQueryChange(action.query)
            is ExpenseListAction.OnCategoryFilterSelect -> onCategoryFilterSelect(action.categoryId)
            ExpenseListAction.OnFiltersClick -> _state.update { it.copy(isFilterSheetVisible = true) }
            ExpenseListAction.OnDismissFilterSheet -> _state.update { it.copy(isFilterSheetVisible = false) }
            is ExpenseListAction.OnDateRangePresetSelect -> onDateRangePresetSelect(action.preset)
            is ExpenseListAction.OnExpenseClick -> navigateToEditor(action.expenseId)
            ExpenseListAction.OnAddExpenseClick -> navigateToEditor(expenseId = null)
            is ExpenseListAction.OnDeleteExpense -> deleteExpense(action.expenseId)
            ExpenseListAction.OnUndoDeleteClick -> undoDelete()
            ExpenseListAction.OnRetryClick -> filterParams.update { it.copy(retryTick = it.retryTick + 1) }
        }
    }

    private fun onSearchQueryChange(query: String) {
        _state.update { it.copy(searchQuery = query) }
        filterParams.update { it.copy(merchant = query.trim().ifBlank { null }) }
    }

    private fun onCategoryFilterSelect(categoryId: Long?) {
        _state.update { it.copy(selectedCategoryId = categoryId) }
        filterParams.update { it.copy(categoryId = categoryId) }
    }

    private fun onDateRangePresetSelect(preset: DateRangePreset) {
        _state.update { it.copy(dateRangePreset = preset, isFilterSheetVisible = false) }
        filterParams.update { it.copy(preset = preset) }
    }

    private fun navigateToEditor(expenseId: Long?) {
        viewModelScope.launch {
            _events.send(ExpenseListEvent.NavigateToExpenseEditor(expenseId))
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeExpenses() {
        viewModelScope.launch {
            filterParams
                .flatMapLatest { params ->
                    val range = params.preset.toDateRange()
                    combine(
                        expenseRepository.observeExpenses(
                            ExpenseFilter(
                                startDate = range.start,
                                endDate = range.endInclusive,
                                categoryId = params.categoryId,
                                merchant = params.merchant,
                            ),
                        ),
                        categoryRepository.observeCategories(),
                    ) { expenses, categories -> expenses to categories }
                }.catch { emitLoadError() }
                .collect { (expenses, categories) ->
                    expensesById = expenses.associateBy { it.id }
                    val categoriesById = categories.associateBy { it.id }
                    _state.update {
                        it.copy(
                            dayGroups = expenses.toDayGroups(categoriesById),
                            categories = categories,
                            isLoading = false,
                            error = null,
                        )
                    }
                }
        }
    }

    private suspend fun emitLoadError() {
        val message = DataError.Local.UNKNOWN.toUiText()
        _state.update { it.copy(isLoading = false, error = message) }
        _events.send(ExpenseListEvent.ShowError(message))
    }

    private fun deleteExpense(expenseId: Long) {
        val expense = expensesById[expenseId] ?: return
        viewModelScope.launch {
            expenseRepository
                .deleteExpense(expense)
                .onSuccess {
                    pendingUndoExpense = expense
                    _events.send(ExpenseListEvent.ShowUndoDeleteSnackbar(expense.merchant))
                }.onFailure { error ->
                    _events.send(ExpenseListEvent.ShowError(error.toUiText()))
                }
        }
    }

    private fun undoDelete() {
        val expense = pendingUndoExpense ?: return
        pendingUndoExpense = null
        viewModelScope.launch {
            expenseRepository
                .addExpense(expense)
                .onFailure { error ->
                    _events.send(ExpenseListEvent.ShowError(error.toUiText()))
                }
        }
    }

    private data class FilterParams(
        val categoryId: Long? = null,
        val merchant: String? = null,
        val preset: DateRangePreset = DateRangePreset.THIS_MONTH,
        val retryTick: Int = 0,
    )
}
