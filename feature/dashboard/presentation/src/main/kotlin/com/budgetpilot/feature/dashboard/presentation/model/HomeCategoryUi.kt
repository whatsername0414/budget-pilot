package com.budgetpilot.feature.dashboard.presentation.model

import com.budgetpilot.core.domain.money.Money

data class HomeCategoryUi(
    val categoryId: Long,
    val name: String,
    val colorKey: String,
    val amount: Money,
    val fraction: Float,
)
