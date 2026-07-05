package com.budgetpilot.core.domain.model

import java.time.LocalDate

data class ExpenseFilter(
    val startDate: LocalDate,
    val endDate: LocalDate,
    val categoryId: Long? = null,
    val merchant: String? = null,
)
