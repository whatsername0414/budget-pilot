package com.budgetpilot.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.vector.ImageVector
import com.budgetpilot.R
import com.budgetpilot.feature.budgets.presentation.navigation.BudgetsRoute
import com.budgetpilot.feature.expenses.presentation.navigation.HistoryRoute
import com.budgetpilot.feature.home.presentation.navigation.HomeRoute
import androidx.compose.material.icons.filled.Home as FilledHome
import androidx.compose.material.icons.outlined.Home as OutlinedHome

/** The four bottom-bar destinations; Capture is reached via the FAB, not the bar. */
enum class TopLevelDestination(
    val route: Any,
    @param:StringRes val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
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
        route = AskRoute,
        labelRes = R.string.nav_ask,
        selectedIcon = NavIcons.ChatFilled,
        unselectedIcon = NavIcons.ChatOutlined,
    ),
    BUDGETS(
        route = BudgetsRoute,
        labelRes = R.string.nav_budgets,
        selectedIcon = NavIcons.WalletFilled,
        unselectedIcon = NavIcons.WalletOutlined,
    ),
}
