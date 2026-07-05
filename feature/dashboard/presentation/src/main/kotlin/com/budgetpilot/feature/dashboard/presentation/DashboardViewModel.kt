package com.budgetpilot.feature.dashboard.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgetpilot.core.domain.DataError
import com.budgetpilot.core.domain.budget.BudgetMath
import com.budgetpilot.core.domain.model.Budget
import com.budgetpilot.core.domain.model.Category
import com.budgetpilot.core.domain.model.Expense
import com.budgetpilot.core.domain.model.ExpenseFilter
import com.budgetpilot.core.domain.money.Money
import com.budgetpilot.core.domain.repository.BudgetRepository
import com.budgetpilot.core.domain.repository.CategoryRepository
import com.budgetpilot.core.domain.repository.ExpenseRepository
import com.budgetpilot.core.presentation.toUiText
import com.budgetpilot.feature.dashboard.presentation.model.DashboardBudgetUi
import com.budgetpilot.feature.dashboard.presentation.model.DashboardCategoryUi
import com.budgetpilot.feature.dashboard.presentation.model.toDashboardExpenseUi
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

private const val TOP_CATEGORY_COUNT = 3
private const val WORST_BUDGET_COUNT = 2
private const val RECENT_EXPENSE_COUNT = 3

class DashboardViewModel(
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository,
    private val budgetRepository: BudgetRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(DashboardState())
    val state = _state.asStateFlow()

    private val _events = Channel<DashboardEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private val retryTick = MutableStateFlow(0)

    init {
        observeDashboard()
    }

    fun onAction(action: DashboardAction) {
        when (action) {
            DashboardAction.OnSeeAllExpensesClick -> sendEvent(DashboardEvent.NavigateToExpenseList)
            DashboardAction.OnSeeBudgetsClick -> sendEvent(DashboardEvent.NavigateToBudgets)
            DashboardAction.OnAddExpenseClick -> sendEvent(DashboardEvent.NavigateToAddExpense)
            DashboardAction.OnRetryClick -> retryTick.update { it + 1 }
        }
    }

    private fun sendEvent(event: DashboardEvent) {
        viewModelScope.launch { _events.send(event) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeDashboard() {
        viewModelScope.launch {
            retryTick
                .flatMapLatest {
                    val month = YearMonth.now()
                    combine(
                        expenseRepository.observeExpenses(
                            ExpenseFilter(startDate = month.atDay(1), endDate = month.atEndOfMonth()),
                        ),
                        categoryRepository.observeCategories(),
                        budgetRepository.observeBudgetsForMonth(month.toString()),
                    ) { expenses, categories, budgets -> Triple(expenses, categories, budgets) }
                }.catch { emitLoadError() }
                .collect { (expenses, categories, budgets) -> buildState(expenses, categories, budgets) }
        }
    }

    private fun buildState(
        expenses: List<Expense>,
        categories: List<Category>,
        budgets: List<Budget>,
    ) {
        val month = YearMonth.now()
        val categoriesById = categories.associateBy { it.id }
        val spentByCategory =
            expenses
                .groupBy { it.categoryId }
                .mapValues { (_, group) -> group.fold(Money.ZERO) { acc, expense -> acc + expense.amount } }

        _state.update {
            it.copy(
                month = month,
                totalSpent = expenses.fold(Money.ZERO) { acc, expense -> acc + expense.amount },
                totalBudgeted = budgets.fold(Money.ZERO) { acc, budget -> acc + budget.amount },
                daysLeftInMonth = daysLeftInMonth(month),
                topCategories = buildTopCategories(categoriesById, spentByCategory),
                worstBudgets = buildWorstBudgets(categoriesById, budgets, spentByCategory),
                recentExpenses = buildRecentExpenses(expenses, categoriesById),
                isLoading = false,
                isEmpty = expenses.isEmpty(),
                error = null,
            )
        }
    }

    private fun buildTopCategories(
        categoriesById: Map<Long, Category>,
        spentByCategory: Map<Long, Money>,
    ): List<DashboardCategoryUi> {
        val topAmounts = spentByCategory.entries.sortedByDescending { it.value }.take(TOP_CATEGORY_COUNT)
        val maxAmount = topAmounts.maxOfOrNull { it.value } ?: Money.ZERO
        return topAmounts.mapNotNull { (categoryId, amount) ->
            val category = categoriesById[categoryId] ?: return@mapNotNull null
            DashboardCategoryUi(
                categoryId = categoryId,
                name = category.name,
                colorKey = category.colorKey,
                amount = amount,
                fraction = if (maxAmount > Money.ZERO) (amount.percentOf(maxAmount) / 100.0).toFloat() else 0f,
            )
        }
    }

    private fun buildWorstBudgets(
        categoriesById: Map<Long, Category>,
        budgets: List<Budget>,
        spentByCategory: Map<Long, Money>,
    ): List<DashboardBudgetUi> =
        budgets
            .mapNotNull { budget ->
                val category = categoriesById[budget.categoryId] ?: return@mapNotNull null
                DashboardBudgetUi(
                    categoryId = category.id,
                    name = category.name,
                    spent = spentByCategory[category.id] ?: Money.ZERO,
                    budget = budget.amount,
                )
            }.sortedByDescending { BudgetMath.percentUsed(it.spent, it.budget) }
            .take(WORST_BUDGET_COUNT)

    private fun buildRecentExpenses(
        expenses: List<Expense>,
        categoriesById: Map<Long, Category>,
    ) = expenses
        .sortedWith(compareByDescending<Expense> { it.date }.thenByDescending { it.createdAt })
        .take(RECENT_EXPENSE_COUNT)
        .map { it.toDashboardExpenseUi(categoriesById[it.categoryId]) }

    private suspend fun emitLoadError() {
        _state.update { it.copy(isLoading = false, error = DataError.Local.UNKNOWN.toUiText()) }
    }
}
