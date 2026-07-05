package com.budgetpilot.core.domain.model

import com.budgetpilot.core.domain.money.Money

data class MonthTotal(
    val month: String,
    val total: Money,
)
