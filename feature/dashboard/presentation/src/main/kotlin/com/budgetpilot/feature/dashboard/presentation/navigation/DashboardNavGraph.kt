package com.budgetpilot.feature.dashboard.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.budgetpilot.feature.dashboard.presentation.DashboardRoot

fun NavGraphBuilder.dashboardGraph(
    onSeeAllExpenses: () -> Unit,
    onSeeBudgets: () -> Unit,
    onAddExpense: () -> Unit,
) {
    composable<HomeRoute> {
        DashboardRoot(
            onSeeAllExpenses = onSeeAllExpenses,
            onSeeBudgets = onSeeBudgets,
            onAddExpense = onAddExpense,
        )
    }
}
