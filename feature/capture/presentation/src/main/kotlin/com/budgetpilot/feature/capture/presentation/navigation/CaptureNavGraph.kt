package com.budgetpilot.feature.capture.presentation.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.budgetpilot.feature.capture.presentation.R
import com.budgetpilot.feature.capture.presentation.capture.CaptureScreen

fun NavGraphBuilder.captureGraph(navController: NavController) {
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
    composable<ConfirmExpenseRoute> { backStackEntry ->
        val route: ConfirmExpenseRoute = backStackEntry.toRoute()
        ConfirmExpenseStub(imagePath = route.imagePath)
    }
}

@Composable
private fun ConfirmExpenseStub(
    imagePath: String,
    modifier: Modifier = Modifier,
) {
    Scaffold(modifier = modifier) { innerPadding ->
        Text(
            text = stringResource(R.string.confirm_expense_stub_message, imagePath),
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
        )
    }
}
