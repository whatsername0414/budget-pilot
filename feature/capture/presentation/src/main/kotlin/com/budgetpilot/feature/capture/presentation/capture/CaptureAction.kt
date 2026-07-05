package com.budgetpilot.feature.capture.presentation.capture

sealed interface CaptureAction {
    data object OnCloseClick : CaptureAction

    data object OnTorchToggleClick : CaptureAction

    data class OnInitialPermissionCheck(
        val isGranted: Boolean,
    ) : CaptureAction

    data class OnPermissionRequestResult(
        val isGranted: Boolean,
    ) : CaptureAction

    data object OnOpenSettingsClick : CaptureAction

    data class OnPhotoCaptured(
        val bytes: ByteArray,
    ) : CaptureAction

    data class OnGalleryImageSelected(
        val bytes: ByteArray,
    ) : CaptureAction

    data object OnCaptureError : CaptureAction
}
