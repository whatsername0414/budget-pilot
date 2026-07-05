package com.budgetpilot.feature.budgets.presentation.charts.model

import com.budgetpilot.core.domain.money.Money

data class CategorySpendUi(
    val categoryId: Long,
    val name: String,
    val colorKey: String,
    val amount: Money,
    val fraction: Float,
)
