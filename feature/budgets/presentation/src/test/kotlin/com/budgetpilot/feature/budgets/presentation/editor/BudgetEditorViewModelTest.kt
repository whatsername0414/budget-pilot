package com.budgetpilot.feature.budgets.presentation.editor

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import com.budgetpilot.core.domain.Result
import com.budgetpilot.core.domain.model.Budget
import com.budgetpilot.core.domain.money.Money
import com.budgetpilot.feature.budgets.presentation.budgets.toMonthString
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
class BudgetEditorViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private val month: YearMonth = YearMonth.now()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `saving a new amount adds a budget via the repository`() =
        runTest {
            val budgetRepository = FakeBudgetRepository()
            val viewModel = BudgetEditorViewModel(1, month, budgetRepository, FakeCategoryRepository())

            viewModel.onAction(BudgetEditorAction.OnQuickAmountSelect(Money.fromPesos("6000.00")))
            viewModel.onAction(BudgetEditorAction.OnSaveClick)

            val result = budgetRepository.getBudget(1, month.toMonthString())
            assertThat(result).isInstanceOf(Result.Success::class)
            assertThat((result as Result.Success).data.amount).isEqualTo(Money.fromPesos("6000.00"))
        }

    @Test
    fun `saving an existing budget updates it instead of adding a new one`() =
        runTest {
            val existing = Budget(id = 9, categoryId = 1, month = month.toMonthString(), amount = Money.fromPesos("1000.00"))
            val budgetRepository = FakeBudgetRepository(seedBudgets = listOf(existing))
            val viewModel = BudgetEditorViewModel(1, month, budgetRepository, FakeCategoryRepository())

            viewModel.events.test {
                viewModel.onAction(BudgetEditorAction.OnQuickAmountSelect(Money.fromPesos("3000.00")))
                viewModel.onAction(BudgetEditorAction.OnSaveClick)
                assertThat(awaitItem()).isInstanceOf(BudgetEditorEvent.Dismiss::class)
            }

            val result = budgetRepository.getBudget(1, month.toMonthString()) as Result.Success
            assertThat(result.data.id).isEqualTo(9L)
            assertThat(result.data.amount).isEqualTo(Money.fromPesos("3000.00"))
        }

    @Test
    fun `removing an existing budget deletes it`() =
        runTest {
            val existing = Budget(id = 9, categoryId = 1, month = month.toMonthString(), amount = Money.fromPesos("1000.00"))
            val budgetRepository = FakeBudgetRepository(seedBudgets = listOf(existing))
            val viewModel = BudgetEditorViewModel(1, month, budgetRepository, FakeCategoryRepository())

            viewModel.onAction(BudgetEditorAction.OnRemoveClick)
            viewModel.onAction(BudgetEditorAction.OnConfirmRemoveClick)

            val result = budgetRepository.getBudget(1, month.toMonthString())
            assertThat(result).isInstanceOf(Result.Error::class)
        }

    @Test
    fun `saving a zero amount shows a validation error and does not save`() =
        runTest {
            val budgetRepository = FakeBudgetRepository()
            val viewModel = BudgetEditorViewModel(1, month, budgetRepository, FakeCategoryRepository())

            viewModel.onAction(BudgetEditorAction.OnSaveClick)

            assertThat(viewModel.state.value.amountError).isNotNull()
            assertThat(budgetRepository.getBudget(1, month.toMonthString())).isInstanceOf(Result.Error::class)
        }

    @Test
    fun `loading an existing budget prefills the amount and marks editing`() =
        runTest {
            val existing = Budget(id = 5, categoryId = 1, month = month.toMonthString(), amount = Money.fromPesos("4500.00"))
            val budgetRepository = FakeBudgetRepository(seedBudgets = listOf(existing))
            val viewModel = BudgetEditorViewModel(1, month, budgetRepository, FakeCategoryRepository())

            val state = viewModel.state.value
            assertThat(state.isEditing).isEqualTo(true)
            assertThat(state.displayAmount).isEqualTo(Money.fromPesos("4500.00"))
            assertThat(state.categoryName).isEqualTo("Food")
        }
}
