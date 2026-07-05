package com.budgetpilot.feature.dashboard.presentation

import androidx.compose.runtime.Stable
import com.budgetpilot.core.domain.money.Money
import com.budgetpilot.core.presentation.UiText
import com.budgetpilot.feature.dashboard.presentation.model.HomeBudgetUi
import com.budgetpilot.feature.dashboard.presentation.model.HomeCategoryUi
import com.budgetpilot.feature.dashboard.presentation.model.HomeExpenseUi
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

private val MonthNameFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MMMM", Locale.ENGLISH)

@Stable
data class HomeState(
    val month: YearMonth = YearMonth.now(),
    val totalSpent: Money = Money.ZERO,
    val totalBudgeted: Money = Money.ZERO,
    val daysLeftInMonth: Int = 0,
    val topCategories: List<HomeCategoryUi> = emptyList(),
    val worstBudgets: List<HomeBudgetUi> = emptyList(),
    val recentExpenses: List<HomeExpenseUi> = emptyList(),
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
