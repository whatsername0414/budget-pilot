package com.budgetpilot.feature.capture.domain

interface ExtractionCache {
    suspend fun get(imageHash: String): String?

    suspend fun put(
        imageHash: String,
        resultJson: String,
    )
}
