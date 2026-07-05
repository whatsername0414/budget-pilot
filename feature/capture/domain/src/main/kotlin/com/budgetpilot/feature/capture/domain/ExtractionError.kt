package com.budgetpilot.feature.capture.domain

import com.budgetpilot.core.domain.Error

sealed interface ExtractionError : Error {
    data object NothingRecognized : ExtractionError

    data object ImageUnreadable : ExtractionError

    data object Cancelled : ExtractionError

    sealed interface Cloud : ExtractionError {
        data object RateLimited : Cloud

        data object Network : Cloud

        data object Unavailable : Cloud
    }
}
