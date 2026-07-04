package com.budgetpilot.ui

import androidx.compose.foundation.layout.Spacer
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
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.budgetpilot.R
import com.budgetpilot.core.designsystem.theme.BudgetPilotTheme
import com.budgetpilot.navigation.AskRoute
import com.budgetpilot.navigation.BudgetsRoute
import com.budgetpilot.navigation.CaptureRoute
import com.budgetpilot.navigation.HistoryRoute
import com.budgetpilot.navigation.HomeRoute
import com.budgetpilot.navigation.TopLevelDestination

// Scaffold keeps 16dp between the FAB and the bottom bar; shifting the 56dp FAB
// down by 16 + 56/2 centers it on the bar's top edge, over the gap left in the bar.
private val FabDockOffset = 44.dp

@Composable
fun AppShell(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    Scaffold(
        modifier = modifier,
        bottomBar = { AppBottomBar(navController) },
        floatingActionButton = {
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
        },
        floatingActionButtonPosition = FabPosition.Center,
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = HomeRoute,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            composable<HomeRoute> {
                PlaceholderScreen(
                    screenName = stringResource(TopLevelDestination.HOME.labelRes),
                    icon = TopLevelDestination.HOME.unselectedIcon,
                )
            }
            composable<HistoryRoute> {
                PlaceholderScreen(
                    screenName = stringResource(TopLevelDestination.HISTORY.labelRes),
                    icon = TopLevelDestination.HISTORY.unselectedIcon,
                )
            }
            composable<AskRoute> {
                PlaceholderScreen(
                    screenName = stringResource(TopLevelDestination.ASK.labelRes),
                    icon = TopLevelDestination.ASK.unselectedIcon,
                )
            }
            composable<BudgetsRoute> {
                PlaceholderScreen(
                    screenName = stringResource(TopLevelDestination.BUDGETS.labelRes),
                    icon = TopLevelDestination.BUDGETS.unselectedIcon,
                )
            }
            composable<CaptureRoute> {
                PlaceholderScreen(
                    screenName = stringResource(R.string.nav_capture),
                    icon = Icons.Filled.Add,
                )
            }
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
            val selected = currentDestination?.hierarchy?.any {
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
                        imageVector = if (selected) {
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
