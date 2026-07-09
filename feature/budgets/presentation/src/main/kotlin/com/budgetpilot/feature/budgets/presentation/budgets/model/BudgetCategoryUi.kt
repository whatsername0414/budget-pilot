package com.budgetpilot.feature.budgets.presentation.budgets.model

import com.budgetpilot.core.domain.money.Money

data class BudgetCategoryUi(
    val categoryId: Long,
    val name: String,
    val iconKey: String,
    val colorKey: String,
    val spent: Money,
    val budget: Money,
)
