package com.budgetpilot.core.domain.model

import com.budgetpilot.core.domain.money.Money

data class Budget(
    val id: Long,
    val categoryId: Long,
    val month: String,
    val amount: Money,
)
