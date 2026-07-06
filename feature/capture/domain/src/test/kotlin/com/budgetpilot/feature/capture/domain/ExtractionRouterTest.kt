package com.budgetpilot.feature.capture.domain

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.budgetpilot.core.domain.Result
import com.budgetpilot.core.domain.money.Money
import com.budgetpilot.feature.capture.domain.model.Confidence
import com.budgetpilot.feature.capture.domain.model.ExtractedField
import com.budgetpilot.feature.capture.domain.model.ExtractedReceipt
import com.budgetpilot.feature.capture.domain.model.LineItem
import com.budgetpilot.feature.capture.domain.model.ReceiptType
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import java.time.LocalDate

class ExtractionRouterTest {
    private val cloudReceipt = fakeReceipt("Jollibee")
    private val onDeviceReceipt = fakeReceipt("SM Supermarket")

    @Test
    fun `routes to cloud when online and privacy allows cloud AI`() =
        runTest {
            val router = router(online = true, cloudAllowed = true)

            val result = router.extract(image())

            assertThat(result).isEqualTo(Result.Success(cloudReceipt))
        }

    @Test
    fun `routes to on-device when online but privacy mode is on`() =
        runTest {
            val router = router(online = true, cloudAllowed = false)

            val result = router.extract(image())

            assertThat(result).isEqualTo(Result.Success(onDeviceReceipt))
        }

    @Test
    fun `routes to on-device when offline even though privacy allows cloud AI`() =
        runTest {
            val router = router(online = false, cloudAllowed = true)

            val result = router.extract(image())

            assertThat(result).isEqualTo(Result.Success(onDeviceReceipt))
        }

    @Test
    fun `routes to on-device when offline and privacy mode is on`() =
        runTest {
            val router = router(online = false, cloudAllowed = false)

            val result = router.extract(image())

            assertThat(result).isEqualTo(Result.Success(onDeviceReceipt))
        }

    @Test
    fun `passes through a cloud extractor failure unchanged`() =
        runTest {
            val router =
                ExtractionRouter(
                    cloud = FakeReceiptExtractor(Result.Error(ExtractionError.Cloud.RateLimited)),
                    onDevice = FakeReceiptExtractor(Result.Success(onDeviceReceipt)),
                    connectivity = ConnectivityObserver { true },
                    preferences = CloudAiPolicy { true },
                )

            val result = router.extract(image())

            assertThat(result).isEqualTo(Result.Error(ExtractionError.Cloud.RateLimited))
        }

    @Test
    fun `router honors a live toggle change without needing a new instance`() =
        runTest {
            var cloudAllowed = true
            val router =
                ExtractionRouter(
                    cloud = FakeReceiptExtractor(Result.Success(cloudReceipt)),
                    onDevice = FakeReceiptExtractor(Result.Success(onDeviceReceipt)),
                    connectivity = ConnectivityObserver { true },
                    preferences = CloudAiPolicy { cloudAllowed },
                )

            assertThat(router.extract(image())).isEqualTo(Result.Success(cloudReceipt))

            cloudAllowed = false

            assertThat(router.extract(image())).isEqualTo(Result.Success(onDeviceReceipt))
        }

    private fun router(
        online: Boolean,
        cloudAllowed: Boolean,
    ): ExtractionRouter =
        ExtractionRouter(
            cloud = FakeReceiptExtractor(Result.Success(cloudReceipt)),
            onDevice = FakeReceiptExtractor(Result.Success(onDeviceReceipt)),
            connectivity = ConnectivityObserver { online },
            preferences = CloudAiPolicy { cloudAllowed },
        )

    private fun image(): ReceiptImage = ReceiptImage(path = "receipt.jpg") { ByteArray(0) }

    private fun fakeReceipt(merchantName: String): ExtractedReceipt =
        ExtractedReceipt(
            merchant = ExtractedField(merchantName, Confidence.HIGH),
            date = ExtractedField(LocalDate.of(2026, 7, 5), Confidence.HIGH),
            lineItems = ExtractedField(listOf(LineItem("Item", Money.ofCentavos(1_000))), Confidence.MEDIUM),
            total = ExtractedField(Money.ofCentavos(1_000), Confidence.HIGH),
            suggestedCategory = ExtractedField(null, Confidence.LOW),
            receiptType = ExtractedField(ReceiptType.PAPER, Confidence.HIGH),
        )

    private class FakeReceiptExtractor(
        private val result: Result<ExtractedReceipt, ExtractionError>,
    ) : ReceiptExtractor {
        override suspend fun extract(image: ReceiptImage): Result<ExtractedReceipt, ExtractionError> = result
    }
}
