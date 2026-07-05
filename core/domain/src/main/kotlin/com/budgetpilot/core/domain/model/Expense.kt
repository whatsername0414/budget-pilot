package com.budgetpilot.core.domain.model

import com.budgetpilot.core.domain.money.Money
import java.time.Instant
import java.time.LocalDate

data class Expense(
    val id: Long,
    val amount: Money,
    val merchant: String,
    val categoryId: Long,
    val date: LocalDate,
    val note: String?,
    val source: ExpenseSource,
    val imageUri: String?,
    val createdAt: Instant,
)
