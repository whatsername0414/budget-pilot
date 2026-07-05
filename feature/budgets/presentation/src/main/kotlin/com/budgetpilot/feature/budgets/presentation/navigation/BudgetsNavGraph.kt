package com.budgetpilot.feature.budgets.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.budgetpilot.feature.budgets.presentation.BudgetsTabScreen

fun NavGraphBuilder.budgetsGraph() {
    composable<BudgetsRoute> {
        BudgetsTabScreen()
    }
}
