package com.budgetpilot.feature.settings.presentation.demo

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isGreaterThan
import assertk.assertions.isInstanceOf
import assertk.assertions.isTrue
import com.budgetpilot.core.domain.DataError
import com.budgetpilot.core.domain.Result
import com.budgetpilot.core.domain.money.Money
import com.budgetpilot.feature.settings.presentation.fake.FakeBudgetRepository
import com.budgetpilot.feature.settings.presentation.fake.FakeCategoryRepository
import com.budgetpilot.feature.settings.presentation.fake.FakeExpenseRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneOffset

class DemoDataSeederTest {
    private val foodCategoryId = FakeCategoryRepository.DefaultCategories.first { it.name == "Food" }.id

    private fun clockOn(date: LocalDate): Clock = Clock.fixed(date.atStartOfDay(ZoneOffset.UTC).toInstant(), ZoneOffset.UTC)

    @Test
    fun `seeding on the first day of the month still puts Food over its budget`() =
        runTest {
            val expenseRepository = FakeExpenseRepository()
            val budgetRepository = FakeBudgetRepository()
            val seeder =
                DemoDataSeeder(
                    expenseRepository = expenseRepository,
                    budgetRepository = budgetRepository,
                    categoryRepository = FakeCategoryRepository(),
                    clock = clockOn(LocalDate.of(2026, 7, 1)),
                )

            val result = seeder.seed()

            assertThat(result).isInstanceOf(Result.Success::class)
            val currentMonth = YearMonth.of(2026, 7).toString()
            val foodSpend =
                expenseRepository.allExpenses
                    .filter { it.categoryId == foodCategoryId && YearMonth.from(it.date).toString() == currentMonth }
                    .fold(Money.ZERO) { acc, expense -> acc + expense.amount }
            val foodBudget = (budgetRepository.getBudget(foodCategoryId, currentMonth) as Result.Success).data.amount

            assertThat(foodSpend > foodBudget).isTrue()
        }

    @Test
    fun `seeding mid-month produces expenses for both the previous and current month`() =
        runTest {
            val expenseRepository = FakeExpenseRepository()
            val seeder =
                DemoDataSeeder(
                    expenseRepository = expenseRepository,
                    budgetRepository = FakeBudgetRepository(),
                    categoryRepository = FakeCategoryRepository(),
                    clock = clockOn(LocalDate.of(2026, 7, 15)),
                )

            seeder.seed()

            val months = expenseRepository.allExpenses.map { YearMonth.from(it.date) }.toSet()
            assertThat(months.contains(YearMonth.of(2026, 6))).isTrue()
            assertThat(months.contains(YearMonth.of(2026, 7))).isTrue()
            assertThat(expenseRepository.allExpenses.size).isGreaterThan(20)
        }

    @Test
    fun `re-seeding replaces the previous run instead of accumulating duplicates`() =
        runTest {
            val expenseRepository = FakeExpenseRepository()
            val seeder =
                DemoDataSeeder(
                    expenseRepository = expenseRepository,
                    budgetRepository = FakeBudgetRepository(),
                    categoryRepository = FakeCategoryRepository(),
                    clock = clockOn(LocalDate.of(2026, 7, 15)),
                )

            seeder.seed()
            val firstRunCount = expenseRepository.allExpenses.size
            seeder.seed()

            assertThat(expenseRepository.allExpenses.size).isEqualTo(firstRunCount)
        }

    @Test
    fun `a repository failure is propagated instead of being swallowed`() =
        runTest {
            val seeder =
                DemoDataSeeder(
                    expenseRepository = FakeExpenseRepository(failWith = DataError.Local.DISK_FULL),
                    budgetRepository = FakeBudgetRepository(),
                    categoryRepository = FakeCategoryRepository(),
                    clock = clockOn(LocalDate.of(2026, 7, 15)),
                )

            val result = seeder.seed()

            assertThat(result).isInstanceOf(Result.Error::class)
        }
}
