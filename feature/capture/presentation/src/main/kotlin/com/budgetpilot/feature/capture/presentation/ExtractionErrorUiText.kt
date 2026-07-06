package com.budgetpilot.feature.capture.presentation

import com.budgetpilot.core.presentation.UiText
import com.budgetpilot.feature.capture.domain.ExtractionError

fun ExtractionError.toUiText(): UiText {
    val stringResId =
        when (this) {
            ExtractionError.NothingRecognized -> R.string.error_extraction_nothing_recognized
            ExtractionError.ImageUnreadable -> R.string.error_extraction_image_unreadable
            ExtractionError.Cancelled -> R.string.error_extraction_cancelled
            ExtractionError.Cloud.RateLimited -> R.string.error_extraction_rate_limited
            ExtractionError.Cloud.Network -> R.string.error_extraction_network
            ExtractionError.Cloud.Unavailable -> R.string.error_extraction_unavailable
        }
    return UiText.StringResource(stringResId)
}
