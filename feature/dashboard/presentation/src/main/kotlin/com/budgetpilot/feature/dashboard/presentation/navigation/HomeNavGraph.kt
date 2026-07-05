package com.budgetpilot.feature.dashboard.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.budgetpilot.feature.dashboard.presentation.HomeScreen

fun NavGraphBuilder.homeGraph(
    onSeeAllExpenses: () -> Unit,
    onSeeBudgets: () -> Unit,
    onAddExpense: () -> Unit,
) {
    composable<HomeRoute> {
        HomeScreen(
            onSeeAllExpenses = onSeeAllExpenses,
            onSeeBudgets = onSeeBudgets,
            onAddExpense = onAddExpense,
        )
    }
}
