package com.budgetpilot.feature.insights.domain

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import com.budgetpilot.core.domain.money.Money
import com.budgetpilot.feature.insights.domain.model.BudgetSnapshot
import com.budgetpilot.feature.insights.domain.model.CategorySpendHistory
import com.budgetpilot.feature.insights.domain.model.ExpenseSnapshot
import com.budgetpilot.feature.insights.domain.model.InsightData
import com.budgetpilot.feature.insights.domain.model.InsightType
import com.budgetpilot.feature.insights.domain.model.MonthlySpendingSnapshot
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

private const val MONTH = "2026-07"

/** "Today" is fixed to 2026-07-19 unless a test overrides the clock. */
class InsightRuleEngineTest {
    private val day19Clock = Clock.fixed(Instant.parse("2026-07-19T12:00:00Z"), ZoneOffset.UTC)
    private val day20Clock = Clock.fixed(Instant.parse("2026-07-20T12:00:00Z"), ZoneOffset.UTC)
    private val engine = InsightRuleEngine(day19Clock)

    private fun budgetOf(
        percentUsed: Int,
        categoryId: Long = 1,
        categoryName: String = "Food",
    ): BudgetSnapshot {
        val budget = Money.ofCentavos(10_000_00)
        val spent = Money.ofCentavos(budget.centavos * percentUsed / 100)
        return BudgetSnapshot(categoryId, categoryName, budget, spent)
    }

    @Test
    fun `empty snapshot produces nothing`() {
        val result = engine.evaluate(MonthlySpendingSnapshot(month = MONTH))
        assertThat(result).isNull()
    }

    @Test
    fun `budget at exactly 80 percent before the 20th fires near-limit`() {
        val snapshot = MonthlySpendingSnapshot(month = MONTH, budgets = listOf(budgetOf(percentUsed = 80)))
        val result = engine.evaluate(snapshot)
        assertThat(result?.type).isEqualTo(InsightType.BUDGET_NEAR_LIMIT)
    }

    @Test
    fun `budget just under 80 percent does not fire`() {
        val snapshot = MonthlySpendingSnapshot(month = MONTH, budgets = listOf(budgetOf(percentUsed = 79)))
        val result = engine.evaluate(snapshot)
        assertThat(result).isNull()
    }

    @Test
    fun `budget at 80 percent on day 19 fires near-limit`() {
        val engineOnDay19 = InsightRuleEngine(day19Clock)
        val snapshot = MonthlySpendingSnapshot(month = MONTH, budgets = listOf(budgetOf(percentUsed = 80)))
        val result = engineOnDay19.evaluate(snapshot)
        assertThat(result?.type).isEqualTo(InsightType.BUDGET_NEAR_LIMIT)
    }

    @Test
    fun `budget at 80 percent on day 20 does not fire near-limit`() {
        val engineOnDay20 = InsightRuleEngine(day20Clock)
        val snapshot = MonthlySpendingSnapshot(month = MONTH, budgets = listOf(budgetOf(percentUsed = 80)))
        val result = engineOnDay20.evaluate(snapshot)
        assertThat(result).isNull()
    }

    @Test
    fun `budget at exactly 100 percent fires exceeded regardless of day`() {
        val engineOnDay20 = InsightRuleEngine(day20Clock)
        val snapshot = MonthlySpendingSnapshot(month = MONTH, budgets = listOf(budgetOf(percentUsed = 100)))
        val result = engineOnDay20.evaluate(snapshot)
        assertThat(result?.type).isEqualTo(InsightType.BUDGET_EXCEEDED)
    }

    @Test
    fun `budget over 100 percent fires exceeded`() {
        val snapshot = MonthlySpendingSnapshot(month = MONTH, budgets = listOf(budgetOf(percentUsed = 150)))
        val result = engine.evaluate(snapshot)
        assertThat(result?.type).isEqualTo(InsightType.BUDGET_EXCEEDED)
    }

    @Test
    fun `category spend at exactly 1point5x average does not fire`() {
        val history =
            CategorySpendHistory(
                categoryId = 1,
                categoryName = "Food",
                currentMonthSpend = Money.ofCentavos(1_500_00),
                priorMonthsSpend = listOf(Money.ofCentavos(1_000_00), Money.ofCentavos(1_000_00), Money.ofCentavos(1_000_00)),
            )
        val result = engine.evaluate(MonthlySpendingSnapshot(month = MONTH, categorySpend = listOf(history)))
        assertThat(result).isNull()
    }

    @Test
    fun `category spend just over 1point5x average fires spike`() {
        val history =
            CategorySpendHistory(
                categoryId = 1,
                categoryName = "Food",
                currentMonthSpend = Money.ofCentavos(1_501_00),
                priorMonthsSpend = listOf(Money.ofCentavos(1_000_00), Money.ofCentavos(1_000_00), Money.ofCentavos(1_000_00)),
            )
        val result = engine.evaluate(MonthlySpendingSnapshot(month = MONTH, categorySpend = listOf(history)))
        assertThat(result?.type).isEqualTo(InsightType.CATEGORY_SPIKE)
    }

    @Test
    fun `category spend with no prior months history does not fire`() {
        val history =
            CategorySpendHistory(
                categoryId = 1,
                categoryName = "Food",
                currentMonthSpend = Money.ofCentavos(5_000_00),
                priorMonthsSpend = emptyList(),
            )
        val result = engine.evaluate(MonthlySpendingSnapshot(month = MONTH, categorySpend = listOf(history)))
        assertThat(result).isNull()
    }

    @Test
    fun `single expense at exactly 30 percent of monthly total does not fire`() {
        // Four expenses summing to 1000: three sit at exactly the 30 percent boundary,
        // the fourth is well under it, so nothing in this snapshot should ever fire.
        val expenses =
            listOf(
                ExpenseSnapshot(expenseId = 1, merchant = "Jollibee", amount = Money.ofCentavos(300_00)),
                ExpenseSnapshot(expenseId = 2, merchant = "SM", amount = Money.ofCentavos(300_00)),
                ExpenseSnapshot(expenseId = 3, merchant = "Puregold", amount = Money.ofCentavos(300_00)),
                ExpenseSnapshot(expenseId = 4, merchant = "7-Eleven", amount = Money.ofCentavos(100_00)),
            )
        val result = engine.evaluate(MonthlySpendingSnapshot(month = MONTH, expenses = expenses))
        assertThat(result).isNull()
    }

    @Test
    fun `single expense just over 30 percent of monthly total fires`() {
        val expenses =
            listOf(
                ExpenseSnapshot(expenseId = 1, merchant = "Jollibee", amount = Money.ofCentavos(301_00)),
                ExpenseSnapshot(expenseId = 2, merchant = "SM", amount = Money.ofCentavos(699_00)),
            )
        val result = engine.evaluate(MonthlySpendingSnapshot(month = MONTH, expenses = expenses))
        assertThat(result?.type).isEqualTo(InsightType.LARGE_EXPENSE)
    }

    @Test
    fun `empty expense list produces no large-expense candidate`() {
        val result = engine.evaluate(MonthlySpendingSnapshot(month = MONTH, expenses = emptyList()))
        assertThat(result).isNull()
    }

    @Test
    fun `budget exceeded outranks budget near-limit even at a lower percent`() {
        val exceeded = budgetOf(percentUsed = 100, categoryId = 1, categoryName = "Food")
        val nearLimit = budgetOf(percentUsed = 99, categoryId = 2, categoryName = "Transport")
        val result = engine.evaluate(MonthlySpendingSnapshot(month = MONTH, budgets = listOf(nearLimit, exceeded)))
        assertThat(result?.type).isEqualTo(InsightType.BUDGET_EXCEEDED)
        assertThat((result?.data as? InsightData.BudgetStatus)?.categoryName)
            .isEqualTo("Food")
    }

    @Test
    fun `budget exceeded outranks category spike and large expense`() {
        val exceededBudget = budgetOf(percentUsed = 120)
        val spikeHistory =
            CategorySpendHistory(
                categoryId = 2,
                categoryName = "Transport",
                currentMonthSpend = Money.ofCentavos(5_000_00),
                priorMonthsSpend = listOf(Money.ofCentavos(100_00)),
            )
        val expenses =
            listOf(
                ExpenseSnapshot(expenseId = 1, merchant = "Big Store", amount = Money.ofCentavos(1_000_00)),
                ExpenseSnapshot(expenseId = 2, merchant = "Small Store", amount = Money.ofCentavos(1_00)),
            )
        val snapshot =
            MonthlySpendingSnapshot(
                month = MONTH,
                budgets = listOf(exceededBudget),
                categorySpend = listOf(spikeHistory),
                expenses = expenses,
            )
        val result = engine.evaluate(snapshot)
        assertThat(result?.type).isEqualTo(InsightType.BUDGET_EXCEEDED)
    }

    @Test
    fun `within the same type, the higher-magnitude candidate wins`() {
        val worse = budgetOf(percentUsed = 200, categoryId = 1, categoryName = "Worse")
        val milder = budgetOf(percentUsed = 101, categoryId = 2, categoryName = "Milder")
        val result = engine.evaluate(MonthlySpendingSnapshot(month = MONTH, budgets = listOf(milder, worse)))
        assertThat(result?.type).isEqualTo(InsightType.BUDGET_EXCEEDED)
        assertThat((result?.data as? InsightData.BudgetStatus)?.categoryName)
            .isEqualTo("Worse")
    }
}
