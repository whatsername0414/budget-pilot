package com.budgetpilot.feature.capture.data

import com.budgetpilot.core.domain.Result
import com.budgetpilot.feature.capture.domain.ExtractionError
import com.budgetpilot.feature.capture.domain.PhReceiptParser
import com.budgetpilot.feature.capture.domain.ReceiptExtractor
import com.budgetpilot.feature.capture.domain.ReceiptImage
import com.budgetpilot.feature.capture.domain.model.ExtractedReceipt

/** Offline [ReceiptExtractor] (PLAN.md §5.4/§6): on-device OCR → [PhReceiptParser]'s PH heuristics. */
class MlKitReceiptExtractor(
    private val recognizer: OcrLineRecognizer,
    private val parser: PhReceiptParser,
) : ReceiptExtractor {
    override suspend fun extract(image: ReceiptImage): Result<ExtractedReceipt, ExtractionError> =
        when (val result = recognizer.recognize(image)) {
            is Result.Success -> parser.parse(result.data)
            is Result.Error -> result
        }
}
