package com.budgetpilot.feature.capture.presentation.capture

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.budgetpilot.core.designsystem.theme.BudgetPilotTheme

@Preview
@Composable
private fun CaptureScreenGrantedPreview() {
    BudgetPilotTheme {
        CaptureContent(
            state = CaptureState(permissionStatus = CameraPermissionStatus.GRANTED),
            onAction = {},
        )
    }
}

@Preview
@Composable
private fun CaptureScreenDeniedPreview() {
    BudgetPilotTheme {
        CaptureContent(
            state = CaptureState(permissionStatus = CameraPermissionStatus.DENIED),
            onAction = {},
        )
    }
}
