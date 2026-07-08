package com.budgetpilot.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.budgetpilot.R
import com.budgetpilot.core.designsystem.theme.BudgetPilotTheme
import com.budgetpilot.core.designsystem.theme.Spacing
import com.budgetpilot.feature.ask.presentation.navigation.AskRoute
import com.budgetpilot.feature.ask.presentation.navigation.askGraph
import com.budgetpilot.feature.budgets.presentation.navigation.BudgetsRoute
import com.budgetpilot.feature.budgets.presentation.navigation.budgetsGraph
import com.budgetpilot.feature.capture.presentation.navigation.CaptureRoute
import com.budgetpilot.feature.capture.presentation.navigation.captureGraph
import com.budgetpilot.feature.history.presentation.navigation.EXPENSE_EDITOR_RESULT_KEY
import com.budgetpilot.feature.history.presentation.navigation.ExpenseEditorRoute
import com.budgetpilot.feature.history.presentation.navigation.HistoryRoute
import com.budgetpilot.feature.history.presentation.navigation.historyGraph
import com.budgetpilot.feature.home.presentation.navigation.HomeRoute
import com.budgetpilot.feature.home.presentation.navigation.homeGraph
import com.budgetpilot.feature.insights.presentation.InsightCardHost
import com.budgetpilot.feature.settings.presentation.navigation.SettingsRoute
import com.budgetpilot.feature.settings.presentation.navigation.settingsGraph
import com.budgetpilot.navigation.NavIcons
import com.budgetpilot.navigation.TopLevelDestination
import com.budgetpilot.navigation.navigateToTopLevel

// design/mockups.html .fab: 60dp square, 20dp corner radius collapsed. Expanded
// morphs to a full circle (radius = FabSize / 2), per the 2026-07-07 approved
// deviation from the mockup's center-docked FAB (DESIGN-SPEC.md §2).
private val FabSize = 60.dp
private val FabCollapsedCornerRadius = 20.dp
private val FabIconSize = 26.dp
private val SpeedDialOptionShape = RoundedCornerShape(percent = 50)

// Scrim alpha matches Material's modal-scrim guidance (~32%) — dims the screen
// behind the expanded dial without hiding it entirely.
private const val SCRIM_ALPHA = 0.32f

@Composable
fun AppShell(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    // Capture, Confirm expense, and the expense editor are pushed/modal destinations
    // (DESIGN-SPEC.md §2/§5/§8), not tabs — the bottom bar and FAB are chrome for the
    // four top-level destinations only.
    val currentTopLevelDestination =
        TopLevelDestination.entries.firstOrNull { destination ->
            currentDestination?.hierarchy?.any { it.hasRoute(destination.route::class) } == true
        }
    val isTopLevelDestination = currentTopLevelDestination != null

    var dialExpanded by remember { mutableStateOf(false) }
    // Collapse on any destination change (including the tab switches the scrim
    // itself still lets through) rather than just on push navigation.
    LaunchedEffect(currentDestination) { dialExpanded = false }
    BackHandler(enabled = dialExpanded) { dialExpanded = false }

    Scaffold(
        modifier = modifier,
        // Each screen owns its own Scaffold + AppTopBar, which already consumes the
        // status-bar inset. Without this, Scaffold's default safeDrawing contentWindowInsets
        // (unconsumed here since there's no topBar) doubles that inset on top of AppTopBar's own.
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = { if (isTopLevelDestination) AppBottomBar(navController) },
        floatingActionButton = {
            if (currentTopLevelDestination?.showsFab == true) {
                CaptureSpeedDial(
                    expanded = dialExpanded,
                    onToggleExpand = { dialExpanded = !dialExpanded },
                    onScanReceipt = {
                        dialExpanded = false
                        navController.navigate(CaptureRoute) { launchSingleTop = true }
                    },
                    onEnterManually = {
                        dialExpanded = false
                        navController.navigate(ExpenseEditorRoute())
                    },
                )
            }
        },
        floatingActionButtonPosition = FabPosition.End,
    ) { innerPadding ->
        AppShellNavContent(
            navController = navController,
            innerPadding = innerPadding,
            dialExpanded = dialExpanded,
            onDismissDial = { dialExpanded = false },
        )
    }
}

@Composable
private fun AppShellNavContent(
    navController: NavHostController,
    innerPadding: PaddingValues,
    dialExpanded: Boolean,
    onDismissDial: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
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
                    navController.navigateToTopLevel(HistoryRoute)
                },
                onSeeBudgets = {
                    navController.navigateToTopLevel(BudgetsRoute)
                },
                onAddExpense = { navController.navigate(ExpenseEditorRoute()) },
                onOpenSettings = { navController.navigate(SettingsRoute) },
                insightSlot = {
                    InsightCardHost(
                        onNavigateToAsk = { prefillQuestion ->
                            navController.navigateToTopLevel(AskRoute(prefillQuestion = prefillQuestion))
                        },
                    )
                },
            )
            historyGraph(navController)
            budgetsGraph()
            settingsGraph(navController)
            askGraph(onNavigateToSettings = { navController.navigate(SettingsRoute) })
            captureGraph(
                navController = navController,
                onSaveSuccess = { confirmationMessage ->
                    navController.navigateToTopLevel(HistoryRoute)
                    navController.getBackStackEntry(HistoryRoute).savedStateHandle[EXPENSE_EDITOR_RESULT_KEY] =
                        confirmationMessage
                },
            )
        }
        // Placed inside the body slot (drawn before the bottom bar/FAB slots, so it
        // never covers either) — tapping it is "tap outside the dial".
        if (dialExpanded) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.scrim.copy(alpha = SCRIM_ALPHA))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) { onDismissDial() },
            )
        }
    }
}

@Composable
private fun CaptureSpeedDial(
    expanded: Boolean,
    onToggleExpand: () -> Unit,
    onScanReceipt: () -> Unit,
    onEnterManually: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(Spacing.small),
    ) {
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 }),
        ) {
            SpeedDialOption(
                label = stringResource(R.string.fab_scan_receipt),
                icon = NavIcons.CameraFilled,
                onClick = onScanReceipt,
            )
        }
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 }),
        ) {
            SpeedDialOption(
                label = stringResource(R.string.fab_enter_manually),
                icon = Icons.Filled.Edit,
                onClick = onEnterManually,
            )
        }
        val cornerRadius by
            animateDpAsState(
                targetValue = if (expanded) FabSize / 2 else FabCollapsedCornerRadius,
                label = "captureFabCorner",
            )
        FloatingActionButton(
            onClick = onToggleExpand,
            modifier = Modifier.size(FabSize),
            shape = RoundedCornerShape(cornerRadius),
            // PLAN.md §4.1 assigns the FAB to the primary role, not M3's default primaryContainer.
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ) {
            Crossfade(targetState = expanded, label = "captureFabIcon") { isExpanded ->
                Icon(
                    imageVector = if (isExpanded) Icons.Filled.Close else Icons.Filled.Add,
                    contentDescription =
                        stringResource(
                            if (isExpanded) R.string.cd_fab_collapse else R.string.cd_fab_expand,
                        ),
                    modifier = Modifier.size(FabIconSize),
                )
            }
        }
    }
}

@Composable
private fun SpeedDialOption(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = SpeedDialOptionShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = Spacing.extraSmall,
        shadowElevation = Spacing.extraSmall,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Spacing.medium, vertical = Spacing.small),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.small),
        ) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(20.dp))
            Text(text = label, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun AppBottomBar(navController: NavController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    NavigationBar {
        TopLevelDestination.entries.forEach { destination ->
            val selected =
                currentDestination?.hierarchy?.any {
                    it.hasRoute(destination.route::class)
                } == true
            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigateToTopLevel(destination.route)
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
                label = {
                    Text(
                        text = stringResource(destination.labelRes),
                        fontWeight = if (selected) FontWeight.SemiBold else null,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
            )
        }
    }
}

@Preview
@Composable
private fun AppShellPreview() {
    BudgetPilotTheme {
        AppShell()
    }
}
