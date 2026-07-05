package com.budgetpilot.feature.capture.domain.model

import com.budgetpilot.core.domain.money.Money

data class LineItem(
    val description: String,
    val amount: Money,
)
