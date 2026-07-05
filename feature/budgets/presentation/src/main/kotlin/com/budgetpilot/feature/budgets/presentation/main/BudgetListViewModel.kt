package com.budgetpilot.feature.budgets.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgetpilot.core.domain.DataError
import com.budgetpilot.core.domain.Result
import com.budgetpilot.core.domain.budget.BudgetMath
import com.budgetpilot.core.domain.model.Budget
import com.budgetpilot.core.domain.model.Category
import com.budgetpilot.core.domain.money.Money
import com.budgetpilot.core.domain.repository.BudgetRepository
import com.budgetpilot.core.domain.repository.CategoryRepository
import com.budgetpilot.core.presentation.toUiText
import com.budgetpilot.feature.budgets.presentation.main.model.BudgetCategoryUi
import com.budgetpilot.feature.budgets.presentation.main.model.UnbudgetedCategoryUi
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
import java.time.YearMonth

class BudgetListViewModel(
    private val budgetRepository: BudgetRepository,
    private val categoryRepository: CategoryRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(BudgetListState())
    val state = _state.asStateFlow()

    private val _events = Channel<BudgetListEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private val month = MutableStateFlow(YearMonth.now())
    private val retryTick = MutableStateFlow(0)

    init {
        observeBudgets()
    }

    fun onAction(action: BudgetListAction) {
        when (action) {
            BudgetListAction.OnPreviousMonthClick -> changeMonth(month.value.minusMonths(1))
            BudgetListAction.OnNextMonthClick -> {
                val next = month.value.plusMonths(1)
                if (!next.isAfter(YearMonth.now())) changeMonth(next)
            }
            is BudgetListAction.OnEditBudgetClick ->
                _state.update { it.copy(editingCategoryId = action.categoryId) }
            BudgetListAction.OnDismissEditor -> _state.update { it.copy(editingCategoryId = null) }
            BudgetListAction.OnRetryClick -> retryTick.update { it + 1 }
        }
    }

    private fun changeMonth(newMonth: YearMonth) {
        month.value = newMonth
        _state.update { it.copy(isLoading = true) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeBudgets() {
        viewModelScope.launch {
            combine(month, retryTick) { selectedMonth, _ -> selectedMonth }
                .flatMapLatest { selectedMonth ->
                    combine(
                        budgetRepository.observeBudgetsForMonth(selectedMonth.toMonthString()),
                        categoryRepository.observeCategories(),
                    ) { budgets, categories -> Triple(selectedMonth, budgets, categories) }
                }.catch { emitLoadError() }
                .collect { (selectedMonth, budgets, categories) -> buildState(selectedMonth, budgets, categories) }
        }
    }

    private suspend fun buildState(
        selectedMonth: YearMonth,
        budgets: List<Budget>,
        categories: List<Category>,
    ) {
        val categoriesById = categories.associateBy { it.id }
        val budgetedCategoryIds = budgets.map { it.categoryId }.toSet()

        val budgetRows =
            budgets
                .mapNotNull { budget ->
                    val category = categoriesById[budget.categoryId] ?: return@mapNotNull null
                    val spent =
                        budgetRepository
                            .spentForCategoryInMonth(budget.categoryId, selectedMonth.toMonthString())
                            .valueOrZero()
                    BudgetCategoryUi(
                        categoryId = category.id,
                        name = category.name,
                        iconKey = category.iconKey,
                        colorKey = category.colorKey,
                        spent = spent,
                        budget = budget.amount,
                    )
                }.sortedByDescending { BudgetMath.percentUsed(it.spent, it.budget) }

        val unbudgetedRows =
            categories
                .filterNot { it.id in budgetedCategoryIds }
                .map { category ->
                    UnbudgetedCategoryUi(
                        categoryId = category.id,
                        name = category.name,
                        iconKey = category.iconKey,
                        colorKey = category.colorKey,
                    )
                }

        _state.update {
            it.copy(
                month = selectedMonth,
                totalBudgeted = budgetRows.fold(Money.ZERO) { acc, row -> acc + row.budget },
                totalSpent = budgetRows.fold(Money.ZERO) { acc, row -> acc + row.spent },
                budgetedCategories = budgetRows,
                unbudgetedCategories = unbudgetedRows,
                isLoading = false,
                error = null,
            )
        }
    }

    private suspend fun emitLoadError() {
        val message = DataError.Local.UNKNOWN.toUiText()
        _state.update { it.copy(isLoading = false, error = message) }
        _events.send(BudgetListEvent.ShowError(message))
    }
}

private fun Result<Money, DataError.Local>.valueOrZero(): Money =
    when (this) {
        is Result.Success -> data
        is Result.Error -> Money.ZERO
    }

internal fun YearMonth.toMonthString(): String = toString()
