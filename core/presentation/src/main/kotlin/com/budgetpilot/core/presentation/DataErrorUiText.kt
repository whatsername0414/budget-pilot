package com.budgetpilot.core.presentation

import com.budgetpilot.core.domain.DataError

fun DataError.toUiText(): UiText {
    val stringResId = when (this) {
        DataError.Network.BAD_REQUEST -> R.string.error_bad_request
        DataError.Network.REQUEST_TIMEOUT -> R.string.error_request_timeout
        DataError.Network.UNAUTHORIZED -> R.string.error_unauthorized
        DataError.Network.FORBIDDEN -> R.string.error_forbidden
        DataError.Network.NOT_FOUND -> R.string.error_not_found
        DataError.Network.CONFLICT -> R.string.error_conflict
        DataError.Network.TOO_MANY_REQUESTS -> R.string.error_too_many_requests
        DataError.Network.NO_INTERNET -> R.string.error_no_internet
        DataError.Network.PAYLOAD_TOO_LARGE -> R.string.error_payload_too_large
        DataError.Network.SERVER_ERROR -> R.string.error_server
        DataError.Network.SERVICE_UNAVAILABLE -> R.string.error_service_unavailable
        DataError.Network.SERIALIZATION -> R.string.error_serialization
        DataError.Network.UNKNOWN -> R.string.error_unknown
        DataError.Local.DISK_FULL -> R.string.error_disk_full
        DataError.Local.NOT_FOUND -> R.string.error_not_found
        DataError.Local.UNKNOWN -> R.string.error_unknown
    }
    return UiText.StringResource(stringResId)
}
