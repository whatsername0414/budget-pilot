package com.budgetpilot.feature.budgets.presentation.charts.model

import com.budgetpilot.core.domain.money.Money
import java.time.YearMonth

data class MonthlyTrendPointUi(
    val month: YearMonth,
    val label: String,
    val total: Money,
)
