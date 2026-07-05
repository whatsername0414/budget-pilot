package com.budgetpilot.feature.dashboard.presentation

import androidx.compose.runtime.Stable
import com.budgetpilot.core.domain.money.Money
import com.budgetpilot.core.presentation.UiText
import com.budgetpilot.feature.dashboard.presentation.model.DashboardBudgetUi
import com.budgetpilot.feature.dashboard.presentation.model.DashboardCategoryUi
import com.budgetpilot.feature.dashboard.presentation.model.DashboardExpenseUi
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

private val MonthNameFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MMMM", Locale.ENGLISH)

@Stable
data class DashboardState(
    val month: YearMonth = YearMonth.now(),
    val totalSpent: Money = Money.ZERO,
    val totalBudgeted: Money = Money.ZERO,
    val daysLeftInMonth: Int = 0,
    val topCategories: List<DashboardCategoryUi> = emptyList(),
    val worstBudgets: List<DashboardBudgetUi> = emptyList(),
    val recentExpenses: List<DashboardExpenseUi> = emptyList(),
    val isLoading: Boolean = true,
    val isEmpty: Boolean = false,
    val error: UiText? = null,
) {
    val monthEyebrow: String
        get() = "${month.format(MonthNameFormatter).uppercase(Locale.ENGLISH)} SPENDING"
}

internal fun daysLeftInMonth(
    month: YearMonth,
    today: LocalDate = LocalDate.now(),
): Int = month.lengthOfMonth() - today.dayOfMonth + 1
