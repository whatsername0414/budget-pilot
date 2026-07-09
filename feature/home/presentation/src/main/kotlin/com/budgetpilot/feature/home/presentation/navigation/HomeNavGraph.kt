package com.budgetpilot.feature.home.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.budgetpilot.feature.home.presentation.HomeScreen

fun NavGraphBuilder.homeGraph(
    onSeeAllExpenses: () -> Unit,
    onSeeBudgets: () -> Unit,
    onAddExpense: () -> Unit,
    onOpenSettings: () -> Unit,
    insightSlot: @Composable (Modifier) -> Unit = {},
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
