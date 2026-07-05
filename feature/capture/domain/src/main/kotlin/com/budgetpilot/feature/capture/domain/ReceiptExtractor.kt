package com.budgetpilot.feature.capture.domain

import com.budgetpilot.core.domain.Result
import com.budgetpilot.feature.capture.domain.model.ExtractedReceipt

interface ReceiptExtractor {
    suspend fun extract(image: ReceiptImage): Result<ExtractedReceipt, ExtractionError>
}
