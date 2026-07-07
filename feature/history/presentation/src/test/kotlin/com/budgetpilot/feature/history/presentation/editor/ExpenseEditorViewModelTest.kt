package com.budgetpilot.feature.history.presentation.editor

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isTrue
import com.budgetpilot.core.domain.DataError
import com.budgetpilot.core.domain.Result
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
class ExpenseEditorViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel(
        savedStateHandle: SavedStateHandle = SavedStateHandle(),
        expenseRepository: FakeExpenseRepository = FakeExpenseRepository(),
        categoryRepository: FakeCategoryRepository = FakeCategoryRepository(),
    ) = ExpenseEditorViewModel(savedStateHandle, expenseRepository, categoryRepository)

    private fun seededExpense(id: Long = 1) =
        Expense(
            id = id,
            amount = Money.fromPesos("20.00"),
            merchant = "Grab",
            categoryId = 2,
            date = LocalDate.now(),
            note = null,
            source = ExpenseSource.MANUAL,
            imageUri = null,
            createdAt = Instant.now(),
        )

    // -- Validation matrix: zero / negative / empty amount --

    @Test
    fun `empty amount is invalid`() {
        val viewModel = viewModel()
        viewModel.onAction(ExpenseEditorAction.OnMerchantChange("Jollibee"))
        viewModel.onAction(ExpenseEditorAction.OnCategorySelect(categoryId = 1))

        assertThat(viewModel.state.value.isValid).isFalse()
    }

    @Test
    fun `zero amount is invalid`() {
        val savedStateHandle = SavedStateHandle(mapOf(ExpenseEditorViewModel.KEY_AMOUNT to "0.00"))
        val viewModel = viewModel(savedStateHandle = savedStateHandle)
        viewModel.onAction(ExpenseEditorAction.OnMerchantChange("Jollibee"))
        viewModel.onAction(ExpenseEditorAction.OnCategorySelect(categoryId = 1))

        assertThat(viewModel.state.value.isValid).isFalse()
    }

    @Test
    fun `negative amount is invalid`() {
        val savedStateHandle = SavedStateHandle(mapOf(ExpenseEditorViewModel.KEY_AMOUNT to "-20.00"))
        val viewModel = viewModel(savedStateHandle = savedStateHandle)
        viewModel.onAction(ExpenseEditorAction.OnMerchantChange("Jollibee"))
        viewModel.onAction(ExpenseEditorAction.OnCategorySelect(categoryId = 1))

        assertThat(viewModel.state.value.isValid).isFalse()
    }

    @Test
    fun `positive amount with merchant and category selected is valid`() {
        val savedStateHandle = SavedStateHandle(mapOf(ExpenseEditorViewModel.KEY_AMOUNT to "150.00"))
        val viewModel = viewModel(savedStateHandle = savedStateHandle)
        viewModel.onAction(ExpenseEditorAction.OnMerchantChange("Jollibee"))
        viewModel.onAction(ExpenseEditorAction.OnCategorySelect(categoryId = 1))

        assertThat(viewModel.state.value.isValid).isTrue()
    }

    @Test
    fun `blank merchant blurred shows a required error`() {
        val viewModel = viewModel()
        viewModel.onAction(ExpenseEditorAction.OnMerchantFieldBlur)

        assertThat(viewModel.state.value.merchantError != null).isTrue()
    }

    // -- Merchant suggestion --

    @Test
    fun `known merchant text auto-selects its suggested category`() {
        val viewModel = viewModel()

        viewModel.onAction(ExpenseEditorAction.OnMerchantChange("Jollibee SM North"))

        assertThat(viewModel.state.value.selectedCategoryId).isEqualTo(1L)
    }

    @Test
    fun `manually selecting a category stops future suggestions from overriding it`() {
        val viewModel = viewModel()

        viewModel.onAction(ExpenseEditorAction.OnCategorySelect(categoryId = 2))
        viewModel.onAction(ExpenseEditorAction.OnMerchantChange("Jollibee"))

        assertThat(viewModel.state.value.selectedCategoryId).isEqualTo(2L)
    }

    // -- Save success / failure --

    @Test
    fun `saving a valid new expense navigates back with a confirmation message`() =
        runTest {
            val expenseRepository = FakeExpenseRepository()
            val savedStateHandle = SavedStateHandle(mapOf(ExpenseEditorViewModel.KEY_AMOUNT to "249.00"))
            val viewModel = viewModel(savedStateHandle = savedStateHandle, expenseRepository = expenseRepository)
            viewModel.onAction(ExpenseEditorAction.OnMerchantChange("Jollibee"))

            viewModel.events.test {
                viewModel.onAction(ExpenseEditorAction.OnSaveClick)
                val event = awaitItem()
                assertThat(event).isInstanceOf(ExpenseEditorEvent.NavigateBack::class)
                event as ExpenseEditorEvent.NavigateBack
                assertThat(event.confirmationMessage != null).isTrue()
            }
        }

    @Test
    fun `save failure emits an error event instead of navigating back`() =
        runTest {
            val expenseRepository =
                FakeExpenseRepository().apply {
                    addResult = { Result.Error(DataError.Local.UNKNOWN) }
                }
            val savedStateHandle = SavedStateHandle(mapOf(ExpenseEditorViewModel.KEY_AMOUNT to "249.00"))
            val viewModel = viewModel(savedStateHandle = savedStateHandle, expenseRepository = expenseRepository)
            viewModel.onAction(ExpenseEditorAction.OnMerchantChange("Jollibee"))

            viewModel.events.test {
                viewModel.onAction(ExpenseEditorAction.OnSaveClick)
                val event = awaitItem()
                assertThat(event).isInstanceOf(ExpenseEditorEvent.ShowError::class)
            }
        }

    @Test
    fun `editing an existing expense loads its fields and updates on save`() =
        runTest {
            val expenseRepository = FakeExpenseRepository(seed = listOf(seededExpense(id = 1)))
            val savedStateHandle = SavedStateHandle(mapOf(ExpenseEditorViewModel.KEY_EXPENSE_ID to 1L))
            val viewModel = viewModel(savedStateHandle = savedStateHandle, expenseRepository = expenseRepository)

            assertThat(viewModel.state.value.merchant).isEqualTo("Grab")
            assertThat(viewModel.state.value.mode).isEqualTo(ExpenseEditorMode.EDIT)

            viewModel.events.test {
                viewModel.onAction(ExpenseEditorAction.OnSaveClick)
                val event = awaitItem()
                assertThat(event).isInstanceOf(ExpenseEditorEvent.NavigateBack::class)
            }
        }

    // -- SavedStateHandle restoration --

    @Test
    fun `restored form fields from SavedStateHandle are used instead of blank defaults`() {
        val savedStateHandle =
            SavedStateHandle(
                mapOf(
                    ExpenseEditorViewModel.KEY_AMOUNT to "75.50",
                    ExpenseEditorViewModel.KEY_MERCHANT to "Puregold",
                    ExpenseEditorViewModel.KEY_NOTE to "Weekly groceries",
                ),
            )

        val viewModel = viewModel(savedStateHandle = savedStateHandle)

        assertThat(viewModel.state.value.amountText).isEqualTo("75.50")
        assertThat(viewModel.state.value.merchant).isEqualTo("Puregold")
        assertThat(viewModel.state.value.note).isEqualTo("Weekly groceries")
    }

    @Test
    fun `restored form fields take priority over the loaded expense in edit mode`() =
        runTest {
            val expenseRepository = FakeExpenseRepository(seed = listOf(seededExpense(id = 1)))
            val savedStateHandle =
                SavedStateHandle(
                    mapOf(
                        ExpenseEditorViewModel.KEY_EXPENSE_ID to 1L,
                        ExpenseEditorViewModel.KEY_MERCHANT to "Grab (edited)",
                    ),
                )

            val viewModel = viewModel(savedStateHandle = savedStateHandle, expenseRepository = expenseRepository)

            assertThat(viewModel.state.value.merchant).isEqualTo("Grab (edited)")
        }
}
