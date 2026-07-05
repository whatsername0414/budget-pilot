package com.budgetpilot.feature.capture.presentation.capture

import com.budgetpilot.core.presentation.UiText

sealed interface CaptureEvent {
    data object NavigateBack : CaptureEvent

    data object RequestCameraPermission : CaptureEvent

    data object OpenAppSettings : CaptureEvent

    data class NavigateToConfirm(
        val imagePath: String,
    ) : CaptureEvent

    data class ShowError(
        val message: UiText,
    ) : CaptureEvent
}
