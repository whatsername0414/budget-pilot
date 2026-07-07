package com.budgetpilot.feature.home.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.budgetpilot.feature.home.presentation.HomeScreen

fun NavGraphBuilder.homeGraph(
    onSeeAllExpenses: () -> Unit,
    onSeeBudgets: () -> Unit,
    onAddExpense: () -> Unit,
    onOpenSettings: () -> Unit,
    insightSlot: @Composable () -> Unit = {},
) {
    composable<HomeRoute> {
        HomeScreen(
            onSeeAllExpenses = onSeeAllExpenses,
            onSeeBudgets = onSeeBudgets,
            onAddExpense = onAddExpense,
            onOpenSettings = onOpenSettings,
            insightSlot = insightSlot,
        )
    }
}
