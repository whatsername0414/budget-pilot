package com.budgetpilot.feature.capture.domain.model

data class ExtractedField<T>(
    val value: T,
    val confidence: Confidence,
)
