package com.budgetpilot.feature.expenses.presentation.navigation

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.budgetpilot.feature.expenses.presentation.editor.ExpenseEditorScreen
import com.budgetpilot.feature.expenses.presentation.main.ExpenseListScreen

/**
 * Also used by [com.budgetpilot.feature.capture.presentation.navigation.captureGraph]'s
 * save-success handoff — capture's Confirm screen returns to this same History
 * destination with a snackbar, so it reuses this result key rather than inventing
 * a second round-trip mechanism.
 */
const val EXPENSE_EDITOR_RESULT_KEY = "expense_editor_confirmation"

fun NavGraphBuilder.expensesGraph(navController: NavController) {
    composable<HistoryRoute> { backStackEntry ->
        val confirmationMessage by backStackEntry.savedStateHandle
            .getStateFlow<String?>(EXPENSE_EDITOR_RESULT_KEY, null)
            .collectAsStateWithLifecycle()

        ExpenseListScreen(
            onNavigateToExpenseEditor = { expenseId ->
                navController.navigate(ExpenseEditorRoute(expenseId))
            },
            confirmationMessage = confirmationMessage,
            onConfirmationMessageDismiss = {
                backStackEntry.savedStateHandle[EXPENSE_EDITOR_RESULT_KEY] = null
            },
        )
    }
    composable<ExpenseEditorRoute> {
        ExpenseEditorScreen(
            onNavigateBack = { confirmationMessage ->
                navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.set(EXPENSE_EDITOR_RESULT_KEY, confirmationMessage)
                navController.popBackStack()
            },
        )
    }
}
