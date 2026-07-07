package com.budgetpilot.feature.capture.domain.model

/** One line of on-device OCR output, with the recognizer's own per-line confidence. */
data class OcrLine(
    val text: String,
    val confidence: Float,
)
