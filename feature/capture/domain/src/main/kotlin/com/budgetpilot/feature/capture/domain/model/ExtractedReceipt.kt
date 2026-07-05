package com.budgetpilot.feature.capture.domain.model

import com.budgetpilot.core.domain.money.Money
import java.time.LocalDate

data class ExtractedReceipt(
    val merchant: ExtractedField<String>,
    val date: ExtractedField<LocalDate>,
    val lineItems: ExtractedField<List<LineItem>>,
    val total: ExtractedField<Money>,
    val suggestedCategory: ExtractedField<String?>,
    val receiptType: ExtractedField<ReceiptType>,
)
