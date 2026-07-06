package com.budgetpilot.feature.capture.data.fake

import com.budgetpilot.feature.capture.domain.ExtractionCache

class FakeExtractionCache(
    private val entries: MutableMap<String, String> = mutableMapOf(),
) : ExtractionCache {
    override suspend fun get(imageHash: String): String? = entries[imageHash]

    override suspend fun put(
        imageHash: String,
        resultJson: String,
    ) {
        entries[imageHash] = resultJson
    }
}
