package com.budgetpilot.core.domain.model

import com.budgetpilot.core.domain.money.Money

data class CategoryTotal(
    val categoryId: Long,
    val total: Money,
)
