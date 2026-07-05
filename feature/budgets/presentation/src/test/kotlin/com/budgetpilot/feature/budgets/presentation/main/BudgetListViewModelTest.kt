package com.budgetpilot.feature.budgets.presentation.main

import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import com.budgetpilot.core.domain.model.Budget
import com.budgetpilot.core.domain.money.Money
import com.budgetpilot.feature.budgets.presentation.fake.FakeBudgetRepository
import com.budgetpilot.feature.budgets.presentation.fake.FakeCategoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.YearMonth

@OptIn(ExperimentalCoroutinesApi::class)
class BudgetListViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private val currentMonth: YearMonth = YearMonth.now()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loading budgets combines budget and spend and sorts by percent used descending`() =
        runTest {
            val budgetRepository =
                FakeBudgetRepository(
                    seedBudgets =
                        listOf(
                            // Transport: 20% used
                            Budget(id = 1, categoryId = 2, month = currentMonth.toString(), amount = Money.fromPesos("500.00")),
                            // Food: 90% used — should sort first
                            Budget(id = 2, categoryId = 1, month = currentMonth.toString(), amount = Money.fromPesos("1000.00")),
                        ),
                ).apply {
                    spendByCategoryAndMonth =
                        mapOf(
                            (1L to currentMonth.toString()) to Money.fromPesos("900.00"),
                            (2L to currentMonth.toString()) to Money.fromPesos("100.00"),
                        )
                }
            val viewModel = BudgetListViewModel(budgetRepository, FakeCategoryRepository())

            val state = viewModel.state.value
            assertThat(state.budgetedCategories).hasSize(2)
            assertThat(state.budgetedCategories.first().name).isEqualTo("Food")
            assertThat(state.budgetedCategories.last().name).isEqualTo("Transport")
            assertThat(state.unbudgetedCategories).hasSize(1)
            assertThat(state.unbudgetedCategories.first().name).isEqualTo("Entertainment")
            assertThat(state.totalBudgeted).isEqualTo(Money.fromPesos("1500.00"))
            assertThat(state.totalSpent).isEqualTo(Money.fromPesos("1000.00"))
        }

    @Test
    fun `with no budgets every category is listed as unbudgeted`() =
        runTest {
            val viewModel = BudgetListViewModel(FakeBudgetRepository(), FakeCategoryRepository())

            val state = viewModel.state.value
            assertThat(state.budgetedCategories).isEmpty()
            assertThat(state.unbudgetedCategories).hasSize(3)
            assertThat(state.hasNoBudgets).isTrue()
        }

    @Test
    fun `switching to the previous month loads its budgets and marks it read only`() =
        runTest {
            val previousMonth = currentMonth.minusMonths(1)
            val budgetRepository =
                FakeBudgetRepository(
                    seedBudgets =
                        listOf(
                            Budget(id = 1, categoryId = 1, month = previousMonth.toString(), amount = Money.fromPesos("2000.00")),
                        ),
                )
            val viewModel = BudgetListViewModel(budgetRepository, FakeCategoryRepository())

            assertThat(viewModel.state.value.budgetedCategories).isEmpty()

            viewModel.onAction(BudgetListAction.OnPreviousMonthClick)

            val state = viewModel.state.value
            assertThat(state.budgetedCategories).hasSize(1)
            assertThat(state.isReadOnly).isTrue()
            assertThat(state.canGoToNextMonth).isTrue()
        }

    @Test
    fun `next month click beyond the current month is a no-op`() =
        runTest {
            val viewModel = BudgetListViewModel(FakeBudgetRepository(), FakeCategoryRepository())
            val monthBefore = viewModel.state.value.month

            viewModel.onAction(BudgetListAction.OnNextMonthClick)

            assertThat(viewModel.state.value.month).isEqualTo(monthBefore)
            assertThat(viewModel.state.value.canGoToNextMonth).isFalse()
        }
}
