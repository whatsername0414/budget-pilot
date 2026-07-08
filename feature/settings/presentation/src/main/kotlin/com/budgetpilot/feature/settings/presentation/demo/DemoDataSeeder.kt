package com.budgetpilot.feature.settings.presentation.demo

import com.budgetpilot.core.domain.DataError
import com.budgetpilot.core.domain.EmptyResult
import com.budgetpilot.core.domain.Result
import com.budgetpilot.core.domain.asEmptyResult
import com.budgetpilot.core.domain.model.Budget
import com.budgetpilot.core.domain.model.Category
import com.budgetpilot.core.domain.model.Expense
import com.budgetpilot.core.domain.model.ExpenseFilter
import com.budgetpilot.core.domain.model.ExpenseSource
import com.budgetpilot.core.domain.money.Money
import com.budgetpilot.core.domain.onFailure
import com.budgetpilot.core.domain.repository.BudgetRepository
import com.budgetpilot.core.domain.repository.CategoryRepository
import com.budgetpilot.core.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.first
import java.time.Clock
import java.time.LocalDate
import java.time.YearMonth

/**
 * Seeds ~2 months of realistic expenses plus a handful of budgets, with the current
 * month's Food budget deliberately set below its seeded Food spend — so opening Home
 * right after seeding always surfaces a real over-budget insight card, regardless of
 * what day of the month it's run. Re-running replaces the same date range, so repeated
 * demo/screenshot sessions stay reproducible instead of accumulating duplicates.
 */
class DemoDataSeeder(
    private val expenseRepository: ExpenseRepository,
    private val budgetRepository: BudgetRepository,
    private val categoryRepository: CategoryRepository,
    private val clock: Clock = Clock.systemDefaultZone(),
) {
    suspend fun seed(): EmptyResult<DataError.Local> {
        val today = LocalDate.now(clock)
        val previousMonthStart = today.withDayOfMonth(1).minusMonths(1)
        val categories = categoryRepository.observeCategories().first().associateBy { it.name }

        clearExpenses(previousMonthStart, today).onFailure { return Result.Error(it) }

        val expenses = previousMonthExpenses(previousMonthStart) + currentMonthExpenses(today)
        for (template in expenses) {
            insertExpense(template, categories).onFailure { return Result.Error(it) }
        }

        val budgets = budgetTemplates(YearMonth.from(previousMonthStart), YearMonth.from(today))
        for (template in budgets) {
            upsertBudget(template, categories).onFailure { return Result.Error(it) }
        }

        return Result.Success(Unit)
    }

    private suspend fun clearExpenses(
        startDate: LocalDate,
        endDate: LocalDate,
    ): EmptyResult<DataError.Local> {
        val existing = expenseRepository.observeExpenses(ExpenseFilter(startDate = startDate, endDate = endDate)).first()
        for (expense in existing) {
            expenseRepository.deleteExpense(expense).onFailure { return Result.Error(it) }
        }
        return Result.Success(Unit)
    }

    private suspend fun insertExpense(
        template: SeedExpense,
        categories: Map<String, Category>,
    ): EmptyResult<DataError.Local> {
        val category = categories.getValue(template.categoryName)
        return expenseRepository
            .addExpense(
                Expense(
                    id = 0,
                    amount = Money.fromPesos(template.pesos),
                    merchant = template.merchant,
                    categoryId = category.id,
                    date = template.date,
                    note = null,
                    source = template.source,
                    imageUri = null,
                    createdAt = template.date.atStartOfDay(clock.zone).toInstant(),
                ),
            ).asEmptyResult()
    }

    private suspend fun upsertBudget(
        template: SeedBudget,
        categories: Map<String, Category>,
    ): EmptyResult<DataError.Local> {
        val category = categories.getValue(template.categoryName)
        val amount = Money.fromPesos(template.pesos)
        val existing = (budgetRepository.getBudget(category.id, template.month) as? Result.Success)?.data
        return if (existing != null) {
            budgetRepository.updateBudget(existing.copy(amount = amount))
        } else {
            budgetRepository
                .addBudget(Budget(id = 0, categoryId = category.id, month = template.month, amount = amount))
                .asEmptyResult()
        }
    }
}

private data class SeedExpense(
    val date: LocalDate,
    val merchant: String,
    val categoryName: String,
    val pesos: String,
    val source: ExpenseSource,
)

private data class SeedBudget(
    val month: String,
    val categoryName: String,
    val pesos: String,
)

private fun previousMonthExpenses(monthStart: LocalDate): List<SeedExpense> {
    fun date(day: Int) = monthStart.withDayOfMonth(day.coerceAtMost(monthStart.lengthOfMonth()))
    return listOf(
        SeedExpense(date(day = 1), "Jollibee", "Food", "189.00", ExpenseSource.RECEIPT),
        SeedExpense(date(day = 2), "Grab", "Transport", "245.00", ExpenseSource.RECEIPT),
        SeedExpense(date(day = 3), "Puregold", "Groceries", "1520.75", ExpenseSource.RECEIPT),
        SeedExpense(date(day = 5), "Meralco", "Bills", "2450.00", ExpenseSource.GCASH),
        SeedExpense(date(day = 6), "Globe", "Bills", "1299.00", ExpenseSource.GCASH),
        SeedExpense(date(day = 7), "7-Eleven", "Groceries", "156.50", ExpenseSource.RECEIPT),
        SeedExpense(date(day = 8), "Jollibee", "Food", "245.00", ExpenseSource.RECEIPT),
        SeedExpense(date(day = 9), "Grab", "Transport", "189.00", ExpenseSource.RECEIPT),
        SeedExpense(date(day = 11), "SM Department Store", "Shopping", "1850.00", ExpenseSource.RECEIPT),
        SeedExpense(date(day = 12), "Mercury Drug", "Health", "620.00", ExpenseSource.RECEIPT),
        SeedExpense(date(day = 14), "Chowking", "Food", "178.00", ExpenseSource.RECEIPT),
        SeedExpense(date(day = 15), "Watsons", "Health", "340.00", ExpenseSource.RECEIPT),
        SeedExpense(date(day = 16), "Puregold", "Groceries", "1780.25", ExpenseSource.RECEIPT),
        SeedExpense(date(day = 18), "Grab", "Transport", "210.00", ExpenseSource.RECEIPT),
        SeedExpense(date(day = 20), "Manila Water", "Bills", "850.00", ExpenseSource.MAYA),
        SeedExpense(date(day = 21), "McDonald's", "Food", "265.00", ExpenseSource.RECEIPT),
        SeedExpense(date(day = 23), "Robinsons", "Shopping", "2340.00", ExpenseSource.RECEIPT),
        SeedExpense(date(day = 25), "Jollibee", "Food", "198.00", ExpenseSource.RECEIPT),
        SeedExpense(date(day = 26), "Angkas", "Transport", "120.00", ExpenseSource.RECEIPT),
        SeedExpense(date(day = 28), "SM Cinema", "Entertainment", "450.00", ExpenseSource.MANUAL),
        SeedExpense(date(day = 29), "Mang Inasal", "Food", "165.00", ExpenseSource.RECEIPT),
    )
}

private fun currentMonthExpenses(today: LocalDate): List<SeedExpense> {
    val monthStart = today.withDayOfMonth(1)

    fun date(day: Int) = monthStart.withDayOfMonth(day.coerceAtMost(monthStart.lengthOfMonth()))
    return listOf(
        // Both dated day 1 so Food is over budget from the very first day of the month onward.
        SeedExpense(date(day = 1), "Jollibee", "Food", "320.00", ExpenseSource.RECEIPT),
        SeedExpense(date(day = 1), "McDonald's", "Food", "385.00", ExpenseSource.RECEIPT),
        SeedExpense(date(day = 1), "Grab", "Transport", "180.00", ExpenseSource.RECEIPT),
        SeedExpense(date(day = 2), "Puregold", "Groceries", "980.50", ExpenseSource.RECEIPT),
        SeedExpense(date(day = 3), "Meralco", "Bills", "2680.00", ExpenseSource.GCASH),
        SeedExpense(date(day = 4), "7-Eleven", "Groceries", "145.00", ExpenseSource.RECEIPT),
        SeedExpense(date(day = 5), "Grab", "Transport", "220.00", ExpenseSource.RECEIPT),
        SeedExpense(date(day = 6), "Chowking", "Food", "210.00", ExpenseSource.MANUAL),
        SeedExpense(date(day = 7), "Mercury Drug", "Health", "450.00", ExpenseSource.RECEIPT),
        SeedExpense(date(day = 8), "SM Department Store", "Shopping", "1650.00", ExpenseSource.RECEIPT),
        SeedExpense(date(day = 9), "Jollibee", "Food", "275.00", ExpenseSource.RECEIPT),
        SeedExpense(date(day = 10), "Globe", "Bills", "1299.00", ExpenseSource.MAYA),
    ).filter { !it.date.isAfter(today) }
}

private fun budgetTemplates(
    previousMonth: YearMonth,
    currentMonth: YearMonth,
): List<SeedBudget> =
    listOf(
        SeedBudget(previousMonth.toString(), "Food", "3500.00"),
        SeedBudget(previousMonth.toString(), "Transport", "2500.00"),
        SeedBudget(previousMonth.toString(), "Bills", "8000.00"),
        SeedBudget(previousMonth.toString(), "Groceries", "4000.00"),
        SeedBudget(previousMonth.toString(), "Shopping", "4000.00"),
        // Deliberately tight so seeded Food spend (₱705+ on day 1 alone) always exceeds it.
        SeedBudget(currentMonth.toString(), "Food", "600.00"),
        SeedBudget(currentMonth.toString(), "Transport", "2500.00"),
        SeedBudget(currentMonth.toString(), "Bills", "8000.00"),
        SeedBudget(currentMonth.toString(), "Groceries", "3500.00"),
        SeedBudget(currentMonth.toString(), "Shopping", "3000.00"),
    )
