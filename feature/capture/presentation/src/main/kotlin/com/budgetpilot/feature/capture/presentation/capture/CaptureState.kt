package com.budgetpilot.feature.capture.presentation.capture

data class CaptureState(
    val permissionStatus: CameraPermissionStatus = CameraPermissionStatus.CHECKING,
    val isTorchOn: Boolean = false,
    val isStoringImage: Boolean = false,
)

enum class CameraPermissionStatus {
    CHECKING,
    GRANTED,
    DENIED,
}
