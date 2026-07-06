package com.budgetpilot.feature.insights.domain.model

import com.budgetpilot.core.domain.money.Money

data class BudgetSnapshot(
    val categoryId: Long,
    val categoryName: String,
    val budgetAmount: Money,
    val spentAmount: Money,
)

data class CategorySpendHistory(
    val categoryId: Long,
    val categoryName: String,
    val currentMonthSpend: Money,
    val priorMonthsSpend: List<Money>,
)

data class ExpenseSnapshot(
    val expenseId: Long,
    val merchant: String,
    val amount: Money,
)

data class MonthlySpendingSnapshot(
    val month: String,
    val budgets: List<BudgetSnapshot> = emptyList(),
    val categorySpend: List<CategorySpendHistory> = emptyList(),
    val expenses: List<ExpenseSnapshot> = emptyList(),
)
