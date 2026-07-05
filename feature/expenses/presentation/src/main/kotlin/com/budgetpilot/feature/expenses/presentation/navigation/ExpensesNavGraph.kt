package com.budgetpilot.feature.expenses.presentation.navigation

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.budgetpilot.feature.expenses.presentation.editor.ExpenseEditorRoot
import com.budgetpilot.feature.expenses.presentation.main.ExpenseListRoot

private const val EXPENSE_EDITOR_RESULT_KEY = "expense_editor_confirmation"

fun NavGraphBuilder.expensesGraph(navController: NavController) {
    composable<HistoryRoute> { backStackEntry ->
        val confirmationMessage by backStackEntry.savedStateHandle
            .getStateFlow<String?>(EXPENSE_EDITOR_RESULT_KEY, null)
            .collectAsStateWithLifecycle()

        ExpenseListRoot(
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
        ExpenseEditorRoot(
            onNavigateBack = { confirmationMessage ->
                navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.set(EXPENSE_EDITOR_RESULT_KEY, confirmationMessage)
                navController.popBackStack()
            },
        )
    }
}
