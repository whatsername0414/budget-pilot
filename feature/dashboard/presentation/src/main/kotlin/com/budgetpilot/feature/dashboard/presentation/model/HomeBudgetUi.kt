package com.budgetpilot.feature.dashboard.presentation.model

import com.budgetpilot.core.domain.money.Money

data class HomeBudgetUi(
    val categoryId: Long,
    val name: String,
    val spent: Money,
    val budget: Money,
)
