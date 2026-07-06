package com.budgetpilot.feature.ask.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.budgetpilot.feature.ask.presentation.AskScreen

fun NavGraphBuilder.askGraph(onNavigateToSettings: () -> Unit) {
    composable<AskRoute> {
        AskScreen(onNavigateToSettings = onNavigateToSettings)
    }
}
