package com.budgetpilot.feature.budgets.presentation.charts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgetpilot.core.domain.DataError
import com.budgetpilot.core.domain.Result
import com.budgetpilot.core.domain.model.Category
import com.budgetpilot.core.domain.model.CategoryTotal
import com.budgetpilot.core.domain.model.MonthTotal
import com.budgetpilot.core.domain.money.Money
import com.budgetpilot.core.domain.repository.CategoryRepository
import com.budgetpilot.core.domain.repository.ExpenseRepository
import com.budgetpilot.core.presentation.toUiText
import com.budgetpilot.feature.budgets.presentation.charts.model.CategorySpendUi
import com.budgetpilot.feature.budgets.presentation.charts.model.MonthlyTrendPointUi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

private val MonthAbbrevFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM", Locale.ENGLISH)
private const val TREND_MONTH_COUNT = 6L

class ChartsViewModel(
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(ChartsState())
    val state = _state.asStateFlow()

    private val month = MutableStateFlow(YearMonth.now())
    private val retryTick = MutableStateFlow(0)

    init {
        observeLoads()
    }

    fun onAction(action: ChartsAction) {
        when (action) {
            ChartsAction.OnPreviousMonthClick -> changeMonth(month.value.minusMonths(1))
            ChartsAction.OnNextMonthClick -> {
                val next = month.value.plusMonths(1)
                if (!next.isAfter(YearMonth.now())) changeMonth(next)
            }
            ChartsAction.OnRetryClick -> retryTick.update { it + 1 }
        }
    }

    private fun changeMonth(newMonth: YearMonth) {
        month.value = newMonth
        _state.update { it.copy(isLoading = true) }
    }

    private fun observeLoads() {
        viewModelScope.launch {
            combine(month, retryTick) { selectedMonth, _ -> selectedMonth }
                .collectLatest { selectedMonth -> loadCharts(selectedMonth) }
        }
    }

    private suspend fun loadCharts(selectedMonth: YearMonth) {
        _state.update { it.copy(isLoading = true, error = null) }

        val categories = categoryRepository.observeCategories().first()
        val trendStart = YearMonth.now().minusMonths(TREND_MONTH_COUNT - 1)
        val categoryResult =
            expenseRepository.sumByCategory(selectedMonth.atDay(1), selectedMonth.atEndOfMonth())
        val trendResult =
            expenseRepository.sumByMonth(trendStart.atDay(1), YearMonth.now().atEndOfMonth())

        if (categoryResult is Result.Error) {
            emitError(categoryResult.error)
            return
        }
        if (trendResult is Result.Error) {
            emitError(trendResult.error)
            return
        }

        _state.update {
            it.copy(
                month = selectedMonth,
                categorySpend = buildCategorySpend(categories, (categoryResult as Result.Success).data),
                monthlyTrend = buildMonthlyTrend(trendStart, (trendResult as Result.Success).data),
                isLoading = false,
                error = null,
            )
        }
    }

    private fun buildCategorySpend(
        categories: List<Category>,
        totals: List<CategoryTotal>,
    ): List<CategorySpendUi> {
        val categoriesById = categories.associateBy { it.id }
        val amountsByCategory =
            totals
                .mapNotNull { total -> categoriesById[total.categoryId]?.let { category -> category to total.total } }
                .sortedByDescending { (_, amount) -> amount }
        val maxAmount = amountsByCategory.maxOfOrNull { (_, amount) -> amount } ?: Money.ZERO
        return amountsByCategory.map { (category, amount) ->
            CategorySpendUi(
                categoryId = category.id,
                name = category.name,
                colorKey = category.colorKey,
                amount = amount,
                fraction = if (maxAmount > Money.ZERO) (amount.percentOf(maxAmount) / 100.0).toFloat() else 0f,
            )
        }
    }

    private fun buildMonthlyTrend(
        trendStart: YearMonth,
        totals: List<MonthTotal>,
    ): List<MonthlyTrendPointUi> {
        val totalsByMonth = totals.associateBy { it.month }
        return (0 until TREND_MONTH_COUNT).map { offset ->
            val trendMonth = trendStart.plusMonths(offset)
            MonthlyTrendPointUi(
                month = trendMonth,
                label = trendMonth.format(MonthAbbrevFormatter),
                total = totalsByMonth[trendMonth.toString()]?.total ?: Money.ZERO,
            )
        }
    }

    private fun emitError(error: DataError.Local) {
        _state.update { it.copy(isLoading = false, error = error.toUiText()) }
    }
}
