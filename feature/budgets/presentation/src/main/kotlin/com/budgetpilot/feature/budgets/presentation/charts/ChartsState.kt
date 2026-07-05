package com.budgetpilot.feature.budgets.presentation.charts

import androidx.compose.runtime.Stable
import com.budgetpilot.core.domain.money.Money
import com.budgetpilot.core.presentation.UiText
import com.budgetpilot.feature.budgets.presentation.charts.model.CategorySpendUi
import com.budgetpilot.feature.budgets.presentation.charts.model.MonthlyTrendPointUi
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

private val MonthLabelFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH)
private val MonthNameFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MMMM", Locale.ENGLISH)

@Stable
data class ChartsState(
    val month: YearMonth = YearMonth.now(),
    val categorySpend: List<CategorySpendUi> = emptyList(),
    val monthlyTrend: List<MonthlyTrendPointUi> = emptyList(),
    val isLoading: Boolean = true,
    val error: UiText? = null,
) {
    val monthLabel: String
        get() = month.format(MonthLabelFormatter)

    val canGoToNextMonth: Boolean
        get() = month.isBefore(YearMonth.now())

    val hasCategorySpend: Boolean
        get() = categorySpend.isNotEmpty()

    val hasEnoughTrendData: Boolean
        get() = monthlyTrend.count { it.total > Money.ZERO } >= 2

    val trendCaption: String
        get() = "Tap a point for the exact month total. ${YearMonth.now().format(MonthNameFormatter)} is in progress."
}
