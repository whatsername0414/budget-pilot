package com.budgetpilot.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.budgetpilot.R
import com.budgetpilot.core.designsystem.theme.BudgetPilotTheme
import com.budgetpilot.feature.ask.presentation.navigation.AskRoute
import com.budgetpilot.feature.ask.presentation.navigation.askGraph
import com.budgetpilot.feature.budgets.presentation.navigation.BudgetsRoute
import com.budgetpilot.feature.budgets.presentation.navigation.budgetsGraph
import com.budgetpilot.feature.capture.presentation.navigation.CaptureRoute
import com.budgetpilot.feature.capture.presentation.navigation.captureGraph
import com.budgetpilot.feature.expenses.presentation.navigation.EXPENSE_EDITOR_RESULT_KEY
import com.budgetpilot.feature.expenses.presentation.navigation.ExpenseEditorRoute
import com.budgetpilot.feature.expenses.presentation.navigation.HistoryRoute
import com.budgetpilot.feature.expenses.presentation.navigation.expensesGraph
import com.budgetpilot.feature.home.presentation.navigation.HomeRoute
import com.budgetpilot.feature.home.presentation.navigation.homeGraph
import com.budgetpilot.feature.insights.presentation.InsightCardHost
import com.budgetpilot.feature.settings.presentation.navigation.SettingsRoute
import com.budgetpilot.feature.settings.presentation.navigation.settingsGraph
import com.budgetpilot.navigation.TopLevelDestination

// Scaffold keeps 16dp between the FAB and the bottom bar; shifting the 56dp FAB
// down by 16 + 56/2 centers it on the bar's top edge, over the gap left in the bar.
private val FabDockOffset = 44.dp

@Composable
fun AppShell(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    // Capture, Confirm expense, and the expense editor are pushed/modal destinations
    // (DESIGN-SPEC.md §2/§5/§8), not tabs — the bottom bar and FAB are chrome for the
    // four top-level destinations only.
    val isTopLevelDestination =
        TopLevelDestination.entries.any { destination ->
            currentDestination?.hierarchy?.any { it.hasRoute(destination.route::class) } == true
        }

    Scaffold(
        modifier = modifier,
        // Each screen owns its own Scaffold + AppTopBar, which already consumes the
        // status-bar inset. Without this, Scaffold's default safeDrawing contentWindowInsets
        // (unconsumed here since there's no topBar) doubles that inset on top of AppTopBar's own.
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = { if (isTopLevelDestination) AppBottomBar(navController) },
        floatingActionButton = {
            if (isTopLevelDestination) {
                FloatingActionButton(
                    onClick = {
                        navController.navigate(CaptureRoute) { launchSingleTop = true }
                    },
                    modifier = Modifier.offset(y = FabDockOffset),
                    // PLAN.md §4.1 assigns the FAB to the primary role, not M3's
                    // default primaryContainer.
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = stringResource(R.string.cd_capture_fab),
                    )
                }
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = HomeRoute,
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
        ) {
            homeGraph(
                onSeeAllExpenses = {
                    navController.navigate(HistoryRoute) { launchSingleTop = true }
                },
                onSeeBudgets = {
                    navController.navigate(BudgetsRoute) { launchSingleTop = true }
                },
                onAddExpense = { navController.navigate(ExpenseEditorRoute()) },
                onOpenSettings = { navController.navigate(SettingsRoute) },
                insightSlot = {
                    InsightCardHost(
                        onNavigateToAsk = { prefillQuestion ->
                            navController.navigate(AskRoute(prefillQuestion = prefillQuestion)) {
                                launchSingleTop = true
                            }
                        },
                    )
                },
            )
            expensesGraph(navController)
            budgetsGraph()
            settingsGraph(navController)
            askGraph(onNavigateToSettings = { navController.navigate(SettingsRoute) })
            captureGraph(
                navController = navController,
                onSaveSuccess = { confirmationMessage ->
                    navController.navigate(HistoryRoute) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                    }
                    navController.getBackStackEntry(HistoryRoute).savedStateHandle[EXPENSE_EDITOR_RESULT_KEY] =
                        confirmationMessage
                },
            )
        }
    }
}

@Composable
private fun AppBottomBar(navController: NavController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    NavigationBar {
        TopLevelDestination.entries.forEachIndexed { index, destination ->
            if (index == TopLevelDestination.entries.size / 2) {
                // Gap under the docked Capture FAB.
                Spacer(modifier = Modifier.weight(1f))
            }
            val selected =
                currentDestination?.hierarchy?.any {
                    it.hasRoute(destination.route::class)
                } == true
            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(destination.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        imageVector =
                            if (selected) {
                                destination.selectedIcon
                            } else {
                                destination.unselectedIcon
                            },
                        contentDescription = null,
                    )
                },
                label = { Text(stringResource(destination.labelRes)) },
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun AppShellPreview() {
    BudgetPilotTheme {
        AppShell()
    }
}
