package com.budgetpilot.feature.capture.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.budgetpilot.feature.capture.presentation.capture.CaptureScreen
import com.budgetpilot.feature.capture.presentation.confirm.ConfirmExpenseScreen

fun NavGraphBuilder.captureGraph(
    navController: NavController,
    onSaveSuccess: (confirmationMessage: String) -> Unit,
) {
    composable<CaptureRoute> {
        CaptureScreen(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToConfirm = { imagePath ->
                navController.navigate(ConfirmExpenseRoute(imagePath)) {
                    popUpTo<CaptureRoute> { inclusive = true }
                }
            },
        )
    }
    composable<ConfirmExpenseRoute> {
        ConfirmExpenseScreen(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToRetake = {
                navController.navigate(CaptureRoute) {
                    popUpTo<ConfirmExpenseRoute> { inclusive = true }
                }
            },
            onSaveSuccess = onSaveSuccess,
        )
    }
}
