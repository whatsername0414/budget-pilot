package com.budgetpilot.feature.capture.data

import com.budgetpilot.core.domain.Result
import com.budgetpilot.core.domain.merchant.PhMerchantCatalog
import com.budgetpilot.core.domain.money.Money
import com.budgetpilot.feature.capture.domain.ExtractionError
import com.budgetpilot.feature.capture.domain.ReceiptExtractor
import com.budgetpilot.feature.capture.domain.ReceiptImage
import com.budgetpilot.feature.capture.domain.model.Confidence
import com.budgetpilot.feature.capture.domain.model.ExtractedField
import com.budgetpilot.feature.capture.domain.model.ExtractedReceipt
import com.budgetpilot.feature.capture.domain.model.LineItem
import com.budgetpilot.feature.capture.domain.model.ReceiptType
import kotlinx.coroutines.delay
import java.time.LocalDate

/**
 * Temporary until P3.5 (`VisionLlmExtractor`): returns a canned Jollibee
 * receipt after a fixed delay so the rest of the capture flow (P2.3+) has a
 * real, demoable extraction result to build against ahead of the Gemini
 * integration.
 */
class StubReceiptExtractor : ReceiptExtractor {
    override suspend fun extract(image: ReceiptImage): Result<ExtractedReceipt, ExtractionError> {
        delay(EXTRACTION_DELAY_MS)

        val merchant = "Jollibee"
        val lineItems =
            listOf(
                LineItem("1pc Chickenjoy w/ Rice", Money.fromPesos("89.00")),
                LineItem("Jollibee Spaghetti", Money.fromPesos("65.00")),
                LineItem("Coke Float", Money.fromPesos("45.00")),
            )
        val total = lineItems.fold(Money.ZERO) { acc, item -> acc + item.amount }

        return Result.Success(
            ExtractedReceipt(
                merchant = ExtractedField(merchant, Confidence.MEDIUM),
                date = ExtractedField(LocalDate.now(), Confidence.HIGH),
                lineItems = ExtractedField(lineItems, Confidence.MEDIUM),
                total = ExtractedField(total, Confidence.HIGH),
                suggestedCategory = ExtractedField(PhMerchantCatalog.suggestCategory(merchant), Confidence.MEDIUM),
                receiptType = ExtractedField(ReceiptType.PAPER, Confidence.HIGH),
            ),
        )
    }

    private companion object {
        const val EXTRACTION_DELAY_MS = 800L
    }
}
