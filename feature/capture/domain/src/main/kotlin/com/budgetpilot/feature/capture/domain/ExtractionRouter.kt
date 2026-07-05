package com.budgetpilot.feature.capture.domain

import com.budgetpilot.core.domain.Result
import com.budgetpilot.feature.capture.domain.model.ExtractedReceipt

class ExtractionRouter(
    private val cloud: ReceiptExtractor,
    private val onDevice: ReceiptExtractor,
    private val connectivity: ConnectivityObserver,
    private val preferences: CloudAiPolicy,
) : ReceiptExtractor {
    override suspend fun extract(image: ReceiptImage): Result<ExtractedReceipt, ExtractionError> {
        val useCloud = preferences.isCloudAiAllowed() && connectivity.isOnline()
        return if (useCloud) cloud.extract(image) else onDevice.extract(image)
    }
}
