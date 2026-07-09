package com.budgetpilot.feature.history.presentation.expenses

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.doesNotContain
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isTrue
import com.budgetpilot.core.domain.model.Expense
import com.budgetpilot.core.domain.model.ExpenseSource
import com.budgetpilot.core.domain.money.Money
import com.budgetpilot.feature.history.presentation.fake.FakeCategoryRepository
import com.budgetpilot.feature.history.presentation.fake.FakeExpenseRepository
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

@OptIn(ExperimentalCoroutinesApi::class)
class ExpenseListViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private val today: LocalDate = LocalDate.now().withDayOfMonth(1).plusDays(9)

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
        merchant: String,
        categoryId: Long,
        amount: String = "100.00",
    ) = Expense(
        id = id,
        amount = Money.fromPesos(amount),
        merchant = merchant,
        categoryId = categoryId,
        date = today,
        note = null,
        source = ExpenseSource.MANUAL,
        imageUri = null,
        createdAt = Instant.now(),
    )

    @Test
    fun `loading expenses groups them by day and formats totals`() =
        runTest {
            val expenseRepository =
                FakeExpenseRepository(
                    seed =
                        listOf(
                            expense(1, "Jollibee", categoryId = 1, amount = "150.00"),
                            expense(2, "Grab", categoryId = 2, amount = "80.00"),
                        ),
                )
            val viewModel = ExpenseListViewModel(expenseRepository, FakeCategoryRepository())

            viewModel.state.test {
                val state = awaitItem()
                assertThat(state.dayGroups).hasSize(1)
                assertThat(state.dayGroups.first().expenses).hasSize(2)
                assertThat(state.isLoading).isEqualTo(false)
            }
        }

    @Test
    fun `selecting a category filter narrows the expense list`() =
        runTest {
            val expenseRepository =
                FakeExpenseRepository(
                    seed =
                        listOf(
                            expense(1, "Jollibee", categoryId = 1),
                            expense(2, "Grab", categoryId = 2),
                        ),
                )
            val viewModel = ExpenseListViewModel(expenseRepository, FakeCategoryRepository())

            viewModel.state.test {
                awaitItem()

                viewModel.onAction(ExpenseListAction.OnCategoryFilterSelect(categoryId = 1))

                // Selecting a filter updates `selectedCategoryId` immediately, then the
                // re-queried expense list arrives as a second, separate state emission.
                awaitItem()
                val filtered = awaitItem()
                val merchants = filtered.dayGroups.flatMap { it.expenses }.map { it.merchant }
                assertThat(merchants).contains("Jollibee")
                assertThat(merchants).doesNotContain("Grab")
            }
        }

    @Test
    fun `load failure emits an error event and sets state error`() =
        runTest {
            val expenseRepository =
                FakeExpenseRepository().apply { shouldFailObserve = true }
            val viewModel = ExpenseListViewModel(expenseRepository, FakeCategoryRepository())

            viewModel.events.test {
                val event = awaitItem()
                assertThat(event).isInstanceOf(ExpenseListEvent.ShowError::class)
            }
            assertThat(viewModel.state.value.error != null).isTrue()
        }
}
