package com.budgetpilot.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.budgetpilot.R
import com.budgetpilot.feature.ask.presentation.navigation.AskRoute
import com.budgetpilot.feature.budgets.presentation.navigation.BudgetsRoute
import com.budgetpilot.feature.history.presentation.navigation.HistoryRoute
import com.budgetpilot.feature.home.presentation.navigation.HomeRoute
import androidx.compose.material.icons.filled.Home as FilledHome
import androidx.compose.material.icons.outlined.Home as OutlinedHome

/**
 * The four bottom-bar destinations; Capture is reached via the FAB, not the
 * bar. [showsFab] is false only for Ask, whose input bar already owns the
 * bottom-right of the screen (2026-07-07 FAB speed-dial rework).
 */
enum class TopLevelDestination(
    val route: Any,
    @param:StringRes val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val showsFab: Boolean = true,
) {
    HOME(
        route = HomeRoute,
        labelRes = R.string.nav_home,
        selectedIcon = Icons.Filled.FilledHome,
        unselectedIcon = Icons.Outlined.OutlinedHome,
    ),
    HISTORY(
        route = HistoryRoute,
        labelRes = R.string.nav_history,
        selectedIcon = NavIcons.ReceiptFilled,
        unselectedIcon = NavIcons.ReceiptOutlined,
    ),
    ASK(
        route = AskRoute(),
        labelRes = R.string.nav_ask,
        selectedIcon = NavIcons.ChatFilled,
        unselectedIcon = NavIcons.ChatOutlined,
        showsFab = false,
    ),
    BUDGETS(
        route = BudgetsRoute,
        labelRes = R.string.nav_budgets,
        selectedIcon = NavIcons.WalletFilled,
        unselectedIcon = NavIcons.WalletOutlined,
    ),
}

/**
 * Navigates to a top-level destination. Every call site that navigates to a
 * top-level destination — the bottom bar AND any in-screen shortcut (e.g.
 * Home's "See all"/"Charts" links, Capture's post-save handoff to History) —
 * must use this rather than a plain [NavController.navigate], or the back
 * stack ends up inconsistent: switching tabs works one direction but
 * silently no-ops the other way until a system back reconciles it.
 *
 * Deliberately NOT the official `saveState = true` / `restoreState = true`
 * "multiple back stacks" pattern
 * (https://developer.android.com/guide/navigation/design/multiple-back-stacks):
 * that pattern only saves/restores correctly when each top-level destination
 * is the start of its own nested graph, so the popped state is keyed by that
 * tab's own distinct start-destination id. This app's graph is flat (nested
 * `navigation<T>` graphs whose route equals their start destination crash at
 * runtime — see the 2026-07-05 gotcha), so `graph.findStartDestination().id`
 * is always Home's id for every call site. With `saveState`/`restoreState`,
 * that made every popUpTo — including one-off pushed screens like Capture's
 * `ConfirmExpenseRoute` popped on its way back to History — save/restore
 * against the exact same shared slot, so a later unrelated tab switch could
 * resurrect a stale pushed screen's state instead of the intended tab.
 */
fun NavController.navigateToTopLevel(route: Any) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id)
        launchSingleTop = true
    }
}
