package com.budgetpilot.feature.budgets.presentation.charts

import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.isTrue
import com.budgetpilot.core.domain.model.Expense
import com.budgetpilot.core.domain.model.ExpenseSource
import com.budgetpilot.core.domain.money.Money
import com.budgetpilot.feature.budgets.presentation.charts.model.CategorySpendUi
import com.budgetpilot.feature.budgets.presentation.fake.FakeCategoryRepository
import com.budgetpilot.feature.budgets.presentation.fake.FakeExpenseRepository
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
import java.time.YearMonth

@OptIn(ExperimentalCoroutinesApi::class)
class ChartsViewModelTest {
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

    private fun expense(
        id: Long,
        amount: String,
        categoryId: Long,
        month: YearMonth,
    ) = Expense(
        id = id,
        amount = Money.fromPesos(amount),
        merchant = "Merchant",
        categoryId = categoryId,
        date = month.atDay(1),
        note = null,
        source = ExpenseSource.MANUAL,
        imageUri = null,
        createdAt = Instant.EPOCH,
    )

    @Test
    fun `category spend is sorted descending with fractions relative to the top category`() =
        runTest {
            val expenses =
                listOf(
                    expense(1, "900.00", categoryId = 1, month = currentMonth),
                    expense(2, "100.00", categoryId = 1, month = currentMonth),
                    expense(3, "500.00", categoryId = 2, month = currentMonth),
                )
            val viewModel = ChartsViewModel(FakeExpenseRepository(expenses), FakeCategoryRepository())

            val state = viewModel.state.value
            assertThat(state.categorySpend).isEqualTo(
                listOf(
                    CategorySpendUi(
                        categoryId = 1,
                        name = "Food",
                        colorKey = "food",
                        amount = Money.fromPesos("1000.00"),
                        fraction = 1f,
                    ),
                    CategorySpendUi(
                        categoryId = 2,
                        name = "Transport",
                        colorKey = "transport",
                        amount = Money.fromPesos("500.00"),
                        fraction = 0.5f,
                    ),
                ),
            )
        }

    @Test
    fun `monthly trend always covers the trailing six months, zero-filled where there is no data`() =
        runTest {
            val expenses =
                listOf(
                    expense(1, "1000.00", categoryId = 1, month = currentMonth.minusMonths(2)),
                    expense(2, "2000.00", categoryId = 1, month = currentMonth),
                )
            val viewModel = ChartsViewModel(FakeExpenseRepository(expenses), FakeCategoryRepository())

            val trend = viewModel.state.value.monthlyTrend
            assertThat(trend).hasSize(6)
            assertThat(trend.first().month).isEqualTo(currentMonth.minusMonths(5))
            assertThat(trend.last().month).isEqualTo(currentMonth)
            assertThat(trend.last().total).isEqualTo(Money.fromPesos("2000.00"))
            assertThat(trend[3].total).isEqualTo(Money.fromPesos("1000.00"))
            assertThat(trend[4].total).isEqualTo(Money.ZERO)
        }

    @Test
    fun `with no expenses the screen reports empty category spend and not enough trend data`() =
        runTest {
            val viewModel = ChartsViewModel(FakeExpenseRepository(), FakeCategoryRepository())

            val state = viewModel.state.value
            assertThat(state.categorySpend).isEmpty()
            assertThat(state.hasCategorySpend).isFalse()
            assertThat(state.hasEnoughTrendData).isFalse()
            assertThat(state.isLoading).isFalse()
        }

    @Test
    fun `trend data across two distinct months is enough`() =
        runTest {
            val expenses =
                listOf(
                    expense(1, "500.00", categoryId = 1, month = currentMonth.minusMonths(1)),
                    expense(2, "500.00", categoryId = 1, month = currentMonth),
                )
            val viewModel = ChartsViewModel(FakeExpenseRepository(expenses), FakeCategoryRepository())

            assertThat(viewModel.state.value.hasEnoughTrendData).isTrue()
        }

    @Test
    fun `a failed load surfaces an error message and stops loading`() =
        runTest {
            val expenseRepository = FakeExpenseRepository().apply { shouldFailSumByCategory = true }
            val viewModel = ChartsViewModel(expenseRepository, FakeCategoryRepository())

            val state = viewModel.state.value
            assertThat(state.isLoading).isFalse()
            assertThat(state.error).isNotNull()
        }

    @Test
    fun `retrying after a failure reloads the charts`() =
        runTest {
            val expenseRepository =
                FakeExpenseRepository(listOf(expense(1, "300.00", categoryId = 1, month = currentMonth)))
                    .apply { shouldFailSumByCategory = true }
            val viewModel = ChartsViewModel(expenseRepository, FakeCategoryRepository())
            assertThat(viewModel.state.value.error).isNotNull()

            expenseRepository.shouldFailSumByCategory = false
            viewModel.onAction(ChartsAction.OnRetryClick)

            val state = viewModel.state.value
            assertThat(state.error).isNull()
            assertThat(state.hasCategorySpend).isTrue()
        }

    @Test
    fun `switching to the previous month reloads category spend for that month`() =
        runTest {
            val previousMonth = currentMonth.minusMonths(1)
            val expenses = listOf(expense(1, "750.00", categoryId = 1, month = previousMonth))
            val viewModel = ChartsViewModel(FakeExpenseRepository(expenses), FakeCategoryRepository())

            assertThat(viewModel.state.value.hasCategorySpend).isFalse()

            viewModel.onAction(ChartsAction.OnPreviousMonthClick)

            val state = viewModel.state.value
            assertThat(state.month).isEqualTo(previousMonth)
            assertThat(state.categorySpend.first().amount).isEqualTo(Money.fromPesos("750.00"))
        }

    @Test
    fun `next month click beyond the current month is a no-op`() =
        runTest {
            val viewModel = ChartsViewModel(FakeExpenseRepository(), FakeCategoryRepository())
            val monthBefore = viewModel.state.value.month

            viewModel.onAction(ChartsAction.OnNextMonthClick)

            assertThat(viewModel.state.value.month).isEqualTo(monthBefore)
            assertThat(viewModel.state.value.canGoToNextMonth).isFalse()
        }
}
