package com.budgetpilot.feature.budgets.presentation.main

import androidx.compose.runtime.Stable
import com.budgetpilot.core.domain.money.Money
import com.budgetpilot.core.presentation.UiText
import com.budgetpilot.feature.budgets.presentation.main.model.BudgetCategoryUi
import com.budgetpilot.feature.budgets.presentation.main.model.UnbudgetedCategoryUi
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

private val MonthLabelFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH)

@Stable
data class BudgetListState(
    val month: YearMonth = YearMonth.now(),
    val totalBudgeted: Money = Money.ZERO,
    val totalSpent: Money = Money.ZERO,
    val budgetedCategories: List<BudgetCategoryUi> = emptyList(),
    val unbudgetedCategories: List<UnbudgetedCategoryUi> = emptyList(),
    val editingCategoryId: Long? = null,
    val isLoading: Boolean = true,
    val error: UiText? = null,
) {
    val monthLabel: String
        get() = month.format(MonthLabelFormatter)

    val isReadOnly: Boolean
        get() = month != YearMonth.now()

    val canGoToNextMonth: Boolean
        get() = month.isBefore(YearMonth.now())

    val totalRemaining: Money
        get() = totalBudgeted - totalSpent

    val hasNoBudgets: Boolean
        get() = budgetedCategories.isEmpty()
}
