package com.budgetpilot.feature.insights.domain.model

import com.budgetpilot.core.domain.money.Money

sealed interface InsightData {
    data class BudgetStatus(
        val categoryId: Long,
        val categoryName: String,
        val month: String,
        val spent: Money,
        val budget: Money,
        val percentUsed: Double,
    ) : InsightData

    data class CategorySpike(
        val categoryId: Long,
        val categoryName: String,
        val month: String,
        val currentSpend: Money,
        val averageSpend: Money,
        val ratio: Double,
    ) : InsightData

    data class LargeExpense(
        val expenseId: Long,
        val merchant: String,
        val month: String,
        val amount: Money,
        val monthlyTotal: Money,
        val percentOfMonthlyTotal: Double,
    ) : InsightData
}
