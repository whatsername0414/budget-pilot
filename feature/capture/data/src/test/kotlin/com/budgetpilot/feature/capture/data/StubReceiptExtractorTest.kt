package com.budgetpilot.feature.capture.data

import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import com.budgetpilot.core.domain.Result
import com.budgetpilot.core.domain.money.Money
import com.budgetpilot.feature.capture.domain.ReceiptImage
import com.budgetpilot.feature.capture.domain.model.Confidence
import com.budgetpilot.feature.capture.domain.model.ReceiptType
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import java.time.LocalDate

class StubReceiptExtractorTest {
    private val extractor = StubReceiptExtractor()
    private val image = ReceiptImage(path = "unused") { ByteArray(0) }

    @Test
    fun `returns a canned Jollibee receipt`() =
        runTest {
            val result = extractor.extract(image)

            assertThat(result).isInstanceOf(Result.Success::class)
            val receipt = (result as Result.Success).data

            assertThat(receipt.merchant.value).isEqualTo("Jollibee")
            assertThat(receipt.merchant.confidence).isEqualTo(Confidence.MEDIUM)
            assertThat(receipt.date.value).isEqualTo(LocalDate.now())
            assertThat(receipt.lineItems.value).hasSize(3)
            assertThat(receipt.total.value).isEqualTo(Money.fromPesos("199.00"))
            assertThat(receipt.total.confidence).isEqualTo(Confidence.HIGH)
            assertThat(receipt.suggestedCategory.value).isEqualTo("Food")
            assertThat(receipt.receiptType.value).isEqualTo(ReceiptType.PAPER)
        }

    @Test
    fun `total matches the sum of the line items`() =
        runTest {
            val result = extractor.extract(image) as Result.Success
            val receipt = result.data

            val lineItemTotal = receipt.lineItems.value.fold(Money.ZERO) { acc, item -> acc + item.amount }

            assertThat(receipt.total.value).isEqualTo(lineItemTotal)
        }
}
