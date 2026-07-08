package com.budgetpilot.feature.capture.presentation.fake

import com.budgetpilot.core.domain.Result
import com.budgetpilot.core.domain.merchant.MerchantCatalog
import com.budgetpilot.core.domain.money.Money
import com.budgetpilot.feature.capture.domain.ExtractionError
import com.budgetpilot.feature.capture.domain.ReceiptExtractor
import com.budgetpilot.feature.capture.domain.ReceiptImage
import com.budgetpilot.feature.capture.domain.model.Confidence
import com.budgetpilot.feature.capture.domain.model.ExtractedField
import com.budgetpilot.feature.capture.domain.model.ExtractedReceipt
import com.budgetpilot.feature.capture.domain.model.LineItem
import com.budgetpilot.feature.capture.domain.model.ReceiptType
import java.time.LocalDate

class FakeReceiptExtractor : ReceiptExtractor {
    var result: Result<ExtractedReceipt, ExtractionError> = Result.Success(defaultReceipt())
    var extractCallCount: Int = 0
        private set

    override suspend fun extract(image: ReceiptImage): Result<ExtractedReceipt, ExtractionError> {
        extractCallCount++
        return result
    }

    companion object {
        fun defaultReceipt(): ExtractedReceipt {
            val merchant = "Jollibee"
            val lineItems =
                listOf(
                    LineItem("1pc Chickenjoy w/ Rice", Money.fromPesos("89.00")),
                    LineItem("Jollibee Spaghetti", Money.fromPesos("65.00")),
                )
            val total = lineItems.fold(Money.ZERO) { acc, item -> acc + item.amount }
            return ExtractedReceipt(
                merchant = ExtractedField(merchant, Confidence.MEDIUM),
                date = ExtractedField(LocalDate.of(2026, 7, 1), Confidence.HIGH),
                lineItems = ExtractedField(lineItems, Confidence.MEDIUM),
                total = ExtractedField(total, Confidence.HIGH),
                suggestedCategory = ExtractedField(MerchantCatalog.suggestCategory(merchant), Confidence.MEDIUM),
                receiptType = ExtractedField(ReceiptType.PAPER, Confidence.HIGH),
            )
        }
    }
}
