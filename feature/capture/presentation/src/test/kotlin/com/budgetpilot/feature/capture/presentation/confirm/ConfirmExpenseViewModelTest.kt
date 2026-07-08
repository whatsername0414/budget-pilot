package com.budgetpilot.feature.capture.presentation.confirm

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.isTrue
import com.budgetpilot.core.domain.DataError
import com.budgetpilot.core.domain.Result
import com.budgetpilot.core.domain.money.Money
import com.budgetpilot.feature.capture.domain.ExtractionError
import com.budgetpilot.feature.capture.domain.model.Confidence
import com.budgetpilot.feature.capture.presentation.fake.FakeCategoryRepository
import com.budgetpilot.feature.capture.presentation.fake.FakeExpenseRepository
import com.budgetpilot.feature.capture.presentation.fake.FakeReceiptExtractor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ConfirmExpenseViewModelTest {
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
        imagePath: String = "receipts/0.jpg",
        receiptExtractor: FakeReceiptExtractor = FakeReceiptExtractor(),
        onDeviceExtractor: FakeReceiptExtractor = FakeReceiptExtractor(),
        expenseRepository: FakeExpenseRepository = FakeExpenseRepository(),
        categoryRepository: FakeCategoryRepository = FakeCategoryRepository(),
    ) = ConfirmExpenseViewModel(
        savedStateHandle = SavedStateHandle(mapOf(ConfirmExpenseViewModel.KEY_IMAGE_PATH to imagePath)),
        receiptExtractor = receiptExtractor,
        onDeviceExtractor = onDeviceExtractor,
        expenseRepository = expenseRepository,
        categoryRepository = categoryRepository,
    )

    @Test
    fun `extraction success prefills fields with their extracted confidence`() =
        runTest {
            val viewModel = viewModel()

            val state = viewModel.state.value
            assertThat(state.phase).isEqualTo(ConfirmExpensePhase.LOADED)
            assertThat(state.merchant).isEqualTo("Jollibee")
            assertThat(state.merchantConfidence).isEqualTo(Confidence.MEDIUM)
            assertThat(state.dateConfidence).isEqualTo(Confidence.HIGH)
            assertThat(state.amountText).isEqualTo("154.00")
            assertThat(state.amountConfidence).isEqualTo(Confidence.HIGH)
            assertThat(state.lineItems).isEqualTo(FakeReceiptExtractor.defaultReceipt().lineItems.value)
            assertThat(state.lineItemsConfidence).isEqualTo(Confidence.MEDIUM)
        }

    @Test
    fun `extraction success resolves suggested category by matching name`() =
        runTest {
            val viewModel = viewModel()

            assertThat(viewModel.state.value.selectedCategoryId).isEqualTo(1L)
            assertThat(viewModel.state.value.categoryConfidence).isEqualTo(Confidence.MEDIUM)
            assertThat(viewModel.state.value.isCategoryManuallySelected).isFalse()
        }

    @Test
    fun `extraction failure sets error phase with a mapped message`() =
        runTest {
            val extractor = FakeReceiptExtractor().apply { result = Result.Error(ExtractionError.Cloud.Network) }

            val viewModel = viewModel(receiptExtractor = extractor)

            assertThat(viewModel.state.value.phase).isEqualTo(ConfirmExpensePhase.ERROR)
            assertThat(viewModel.state.value.errorMessage).isNotNull()
        }

    @Test
    fun `retry after extraction failure re-invokes the extractor`() =
        runTest {
            val extractor = FakeReceiptExtractor().apply { result = Result.Error(ExtractionError.Cloud.Network) }
            val viewModel = viewModel(receiptExtractor = extractor)
            assertThat(viewModel.state.value.phase).isEqualTo(ConfirmExpensePhase.ERROR)

            extractor.result = Result.Success(FakeReceiptExtractor.defaultReceipt())
            viewModel.onAction(ConfirmExpenseAction.OnRetryExtractionClick)

            assertThat(extractor.extractCallCount).isEqualTo(2)
            assertThat(viewModel.state.value.phase).isEqualTo(ConfirmExpensePhase.LOADED)
        }

    @Test
    fun `cloud rate-limit failure offers the offline scan fallback`() =
        runTest {
            val extractor = FakeReceiptExtractor().apply { result = Result.Error(ExtractionError.Cloud.RateLimited) }

            val viewModel = viewModel(receiptExtractor = extractor)

            assertThat(viewModel.state.value.canUseOfflineScan).isTrue()
        }

    @Test
    fun `cloud network failure offers the offline scan fallback`() =
        runTest {
            val extractor = FakeReceiptExtractor().apply { result = Result.Error(ExtractionError.Cloud.Network) }

            val viewModel = viewModel(receiptExtractor = extractor)

            assertThat(viewModel.state.value.canUseOfflineScan).isTrue()
        }

    @Test
    fun `a non-cloud extraction failure does not offer the offline scan fallback`() =
        runTest {
            val extractor = FakeReceiptExtractor().apply { result = Result.Error(ExtractionError.ImageUnreadable) }

            val viewModel = viewModel(receiptExtractor = extractor)

            assertThat(viewModel.state.value.canUseOfflineScan).isFalse()
        }

    @Test
    fun `retrying on-device after a cloud failure calls only the on-device extractor`() =
        runTest {
            val cloudExtractor = FakeReceiptExtractor().apply { result = Result.Error(ExtractionError.Cloud.Network) }
            val onDeviceExtractor = FakeReceiptExtractor()
            val viewModel = viewModel(receiptExtractor = cloudExtractor, onDeviceExtractor = onDeviceExtractor)
            assertThat(viewModel.state.value.canUseOfflineScan).isTrue()

            viewModel.onAction(ConfirmExpenseAction.OnRetryOnDeviceClick)

            assertThat(cloudExtractor.extractCallCount).isEqualTo(1)
            assertThat(onDeviceExtractor.extractCallCount).isEqualTo(1)
            assertThat(viewModel.state.value.phase).isEqualTo(ConfirmExpensePhase.LOADED)
            assertThat(viewModel.state.value.merchant).isEqualTo(FakeReceiptExtractor.defaultReceipt().merchant.value)
        }

    @Test
    fun `enter manually after an error clears fields and moves to loaded phase`() =
        runTest {
            val extractor = FakeReceiptExtractor().apply { result = Result.Error(ExtractionError.Cloud.Network) }
            val viewModel = viewModel(receiptExtractor = extractor)

            viewModel.onAction(ConfirmExpenseAction.OnEnterManuallyClick)

            val state = viewModel.state.value
            assertThat(state.phase).isEqualTo(ConfirmExpensePhase.LOADED)
            assertThat(state.merchant).isEqualTo("")
            assertThat(state.amountText).isEqualTo("")
            assertThat(state.lineItems).isEqualTo(emptyList())
            assertThat(state.selectedCategoryId).isNull()
        }

    @Test
    fun `editing merchant clears the merchant confidence flag`() =
        runTest {
            val viewModel = viewModel()
            assertThat(viewModel.state.value.merchantConfidence).isEqualTo(Confidence.MEDIUM)

            viewModel.onAction(ConfirmExpenseAction.OnMerchantChange("Jollibee SM North"))

            assertThat(viewModel.state.value.merchantConfidence).isEqualTo(Confidence.HIGH)
        }

    @Test
    fun `selecting a category manually marks it as manually selected`() =
        runTest {
            val viewModel = viewModel()

            viewModel.onAction(ConfirmExpenseAction.OnCategorySelect(2L))

            val state = viewModel.state.value
            assertThat(state.selectedCategoryId).isEqualTo(2L)
            assertThat(state.isCategoryManuallySelected).isTrue()
            assertThat(state.categoryConfidence).isEqualTo(Confidence.HIGH)
        }

    @Test
    fun `save with valid fields adds the expense and emits navigate home`() =
        runTest {
            val expenseRepository = FakeExpenseRepository()
            val viewModel = viewModel(expenseRepository = expenseRepository)

            viewModel.events.test {
                viewModel.onAction(ConfirmExpenseAction.OnSaveClick)
                val event = awaitItem()
                assertThat(event).isInstanceOf(ConfirmExpenseEvent.NavigateHome::class)
            }

            assertThat(expenseRepository.addedExpenses.single().amount).isEqualTo(Money.fromPesos("154.00"))
            assertThat(expenseRepository.addedExpenses.single().merchant).isEqualTo("Jollibee")
            assertThat(viewModel.state.value.isSaving).isFalse()
        }

    @Test
    fun `save failure emits show error and resets isSaving`() =
        runTest {
            val expenseRepository = FakeExpenseRepository().apply { addResult = Result.Error(DataError.Local.UNKNOWN) }
            val viewModel = viewModel(expenseRepository = expenseRepository)

            viewModel.events.test {
                viewModel.onAction(ConfirmExpenseAction.OnSaveClick)
                assertThat(awaitItem()).isInstanceOf(ConfirmExpenseEvent.ShowError::class)
            }
            assertThat(viewModel.state.value.isSaving).isFalse()
        }

    @Test
    fun `save does nothing when the merchant is blank`() =
        runTest {
            val expenseRepository = FakeExpenseRepository()
            val viewModel = viewModel(expenseRepository = expenseRepository)
            viewModel.onAction(ConfirmExpenseAction.OnMerchantChange(""))

            viewModel.onAction(ConfirmExpenseAction.OnSaveClick)

            assertThat(expenseRepository.addedExpenses).isEqualTo(emptyList())
        }

    @Test
    fun `adding a line item appends it and clears the low-confidence flag`() =
        runTest {
            val viewModel = viewModel()
            assertThat(viewModel.state.value.lineItemsConfidence).isEqualTo(Confidence.MEDIUM)

            viewModel.onAction(ConfirmExpenseAction.OnAddLineItemClick)
            assertThat(viewModel.state.value.editingLineItemIndex).isNull()

            viewModel.onAction(ConfirmExpenseAction.OnLineItemDescriptionChange("Iced Tea"))
            viewModel.onAction(ConfirmExpenseAction.OnLineItemPriceChange("35.00"))
            viewModel.onAction(ConfirmExpenseAction.OnSaveLineItemClick)

            val state = viewModel.state.value
            assertThat(state.isLineItemSheetVisible).isFalse()
            assertThat(state.lineItems.last().description).isEqualTo("Iced Tea")
            assertThat(state.lineItems.last().amount).isEqualTo(Money.fromPesos("35.00"))
            assertThat(state.lineItems.size).isEqualTo(3)
            assertThat(state.lineItemsConfidence).isEqualTo(Confidence.HIGH)
        }

    @Test
    fun `editing a line item prefills the draft and replaces it in place`() =
        runTest {
            val viewModel = viewModel()

            viewModel.onAction(ConfirmExpenseAction.OnEditLineItemClick(0))
            val prefilled = viewModel.state.value
            assertThat(prefilled.editingLineItemIndex).isEqualTo(0)
            assertThat(prefilled.lineItemDraftDescription).isEqualTo("1pc Chickenjoy w/ Rice")
            assertThat(prefilled.lineItemDraftPriceText).isEqualTo("89.00")

            viewModel.onAction(ConfirmExpenseAction.OnLineItemPriceChange("99.00"))
            viewModel.onAction(ConfirmExpenseAction.OnSaveLineItemClick)

            val state = viewModel.state.value
            assertThat(state.lineItems.size).isEqualTo(2)
            assertThat(state.lineItems[0].description).isEqualTo("1pc Chickenjoy w/ Rice")
            assertThat(state.lineItems[0].amount).isEqualTo(Money.fromPesos("99.00"))
        }

    @Test
    fun `removing a line item drops it from the list`() =
        runTest {
            val viewModel = viewModel()

            viewModel.onAction(ConfirmExpenseAction.OnEditLineItemClick(0))
            viewModel.onAction(ConfirmExpenseAction.OnRemoveLineItemClick)

            val state = viewModel.state.value
            assertThat(state.isLineItemSheetVisible).isFalse()
            assertThat(state.lineItems.size).isEqualTo(1)
            assertThat(state.lineItems.single().description).isEqualTo("Jollibee Spaghetti")
        }

    @Test
    fun `saving a line item with a blank name or non-positive price does nothing`() =
        runTest {
            val viewModel = viewModel()
            viewModel.onAction(ConfirmExpenseAction.OnAddLineItemClick)

            viewModel.onAction(ConfirmExpenseAction.OnLineItemDescriptionChange(""))
            viewModel.onAction(ConfirmExpenseAction.OnLineItemPriceChange("35.00"))
            viewModel.onAction(ConfirmExpenseAction.OnSaveLineItemClick)
            assertThat(viewModel.state.value.isLineItemSheetVisible).isTrue()
            assertThat(viewModel.state.value.lineItems.size).isEqualTo(2)

            viewModel.onAction(ConfirmExpenseAction.OnLineItemDescriptionChange("Iced Tea"))
            viewModel.onAction(ConfirmExpenseAction.OnLineItemPriceChange("0.00"))
            viewModel.onAction(ConfirmExpenseAction.OnSaveLineItemClick)
            assertThat(viewModel.state.value.isLineItemSheetVisible).isTrue()
            assertThat(viewModel.state.value.lineItems.size).isEqualTo(2)
        }

    @Test
    fun `dismissing the line item sheet discards the draft`() =
        runTest {
            val viewModel = viewModel()

            viewModel.onAction(ConfirmExpenseAction.OnEditLineItemClick(0))
            viewModel.onAction(ConfirmExpenseAction.OnLineItemDescriptionChange("Something else"))
            viewModel.onAction(ConfirmExpenseAction.OnDismissLineItemSheet)

            val state = viewModel.state.value
            assertThat(state.isLineItemSheetVisible).isFalse()
            assertThat(state.editingLineItemIndex).isNull()
            assertThat(state.lineItems).isEqualTo(FakeReceiptExtractor.defaultReceipt().lineItems.value)
        }
}
