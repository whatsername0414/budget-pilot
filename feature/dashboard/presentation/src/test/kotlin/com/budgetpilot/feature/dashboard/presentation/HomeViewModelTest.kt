package com.budgetpilot.feature.dashboard.presentation

import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import com.budgetpilot.core.domain.model.Budget
import com.budgetpilot.core.domain.model.Expense
import com.budgetpilot.core.domain.model.ExpenseSource
import com.budgetpilot.core.domain.money.Money
import com.budgetpilot.feature.dashboard.presentation.fake.FakeBudgetRepository
import com.budgetpilot.feature.dashboard.presentation.fake.FakeCategoryRepository
import com.budgetpilot.feature.dashboard.presentation.fake.FakeExpenseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private val currentMonth: YearMonth = YearMonth.now()
    private val today: LocalDate = LocalDate.now()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `combines expenses, categories and budgets into month totals, top categories and worst budgets`() =
        runTest {
            val expenseRepository =
                FakeExpenseRepository(
                    seedExpenses =
                        listOf(
                            expense(id = 1, categoryId = 1, amount = "900.00", date = today),
                            expense(id = 2, categoryId = 1, amount = "100.00", date = today),
                            expense(id = 3, categoryId = 2, amount = "500.00", date = today),
                        ),
                )
            val budgetRepository =
                FakeBudgetRepository(
                    seedBudgets =
                        listOf(
                            // Food: 100% used
                            Budget(id = 1, categoryId = 1, month = currentMonth.toString(), amount = Money.fromPesos("1000.00")),
                            // Transport: 50% used
                            Budget(id = 2, categoryId = 2, month = currentMonth.toString(), amount = Money.fromPesos("1000.00")),
                        ),
                )

            val viewModel = HomeViewModel(expenseRepository, FakeCategoryRepository(), budgetRepository)

            val state = viewModel.state.value
            assertThat(state.isLoading).isFalse()
            assertThat(state.isEmpty).isFalse()
            assertThat(state.totalSpent).isEqualTo(Money.fromPesos("1500.00"))
            assertThat(state.totalBudgeted).isEqualTo(Money.fromPesos("2000.00"))

            assertThat(state.topCategories).hasSize(2)
            assertThat(state.topCategories.first().name).isEqualTo("Food")
            assertThat(state.topCategories.first().fraction).isEqualTo(1f)
            assertThat(state.topCategories.last().name).isEqualTo("Transport")

            assertThat(state.worstBudgets).hasSize(2)
            assertThat(state.worstBudgets.first().name).isEqualTo("Food")

            assertThat(state.recentExpenses).hasSize(3)
        }

    @Test
    fun `no expenses this month yields the empty state with zero totals`() =
        runTest {
            val viewModel = HomeViewModel(FakeExpenseRepository(), FakeCategoryRepository(), FakeBudgetRepository())

            val state = viewModel.state.value
            assertThat(state.isEmpty).isTrue()
            assertThat(state.totalSpent).isEqualTo(Money.ZERO)
            assertThat(state.topCategories).isEmpty()
            assertThat(state.recentExpenses).isEmpty()
        }

    @Test
    fun `only counts expenses and budgets from the current month`() =
        runTest {
            val lastMonth = currentMonth.minusMonths(1)
            val expenseRepository =
                FakeExpenseRepository(
                    seedExpenses =
                        listOf(
                            expense(id = 1, categoryId = 1, amount = "300.00", date = today),
                            expense(id = 2, categoryId = 1, amount = "5000.00", date = lastMonth.atEndOfMonth()),
                        ),
                )
            val budgetRepository =
                FakeBudgetRepository(
                    seedBudgets =
                        listOf(
                            Budget(id = 1, categoryId = 1, month = currentMonth.toString(), amount = Money.fromPesos("1000.00")),
                            Budget(id = 2, categoryId = 1, month = lastMonth.toString(), amount = Money.fromPesos("9000.00")),
                        ),
                )

            val viewModel = HomeViewModel(expenseRepository, FakeCategoryRepository(), budgetRepository)

            val state = viewModel.state.value
            assertThat(state.month).isEqualTo(currentMonth)
            assertThat(state.totalSpent).isEqualTo(Money.fromPesos("300.00"))
            assertThat(state.totalBudgeted).isEqualTo(Money.fromPesos("1000.00"))
        }

    private fun expense(
        id: Long,
        categoryId: Long,
        amount: String,
        date: LocalDate,
    ) = Expense(
        id = id,
        amount = Money.fromPesos(amount),
        merchant = "Merchant $id",
        categoryId = categoryId,
        date = date,
        note = null,
        source = ExpenseSource.MANUAL,
        imageUri = null,
        createdAt = Instant.now(),
    )
}
