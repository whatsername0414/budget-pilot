package com.budgetpilot.feature.capture.data

import com.budgetpilot.core.domain.Result
import com.budgetpilot.feature.capture.domain.ExtractionError
import com.budgetpilot.feature.capture.domain.ReceiptImage
import com.budgetpilot.feature.capture.domain.model.OcrLine

/**
 * Isolates the raw on-device OCR call from [MlKitReceiptExtractor] so the extractor's own
 * success/failure handoff to [com.budgetpilot.feature.capture.domain.PhReceiptParser] is unit-testable
 * without ML Kit/Android Bitmap classes, matching the [ReceiptImageScaling] precedent of keeping
 * framework glue out of tested logic.
 */
fun interface OcrLineRecognizer {
    suspend fun recognize(image: ReceiptImage): Result<List<OcrLine>, ExtractionError>
}
