package com.budgetpilot.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.vector.ImageVector
import com.budgetpilot.R
import androidx.compose.material.icons.automirrored.filled.List as FilledList
import androidx.compose.material.icons.automirrored.filled.Send as FilledSend
import androidx.compose.material.icons.automirrored.outlined.List as OutlinedList
import androidx.compose.material.icons.automirrored.outlined.Send as OutlinedSend
import androidx.compose.material.icons.filled.DateRange as FilledDateRange
import androidx.compose.material.icons.filled.Home as FilledHome
import androidx.compose.material.icons.outlined.DateRange as OutlinedDateRange
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
        selectedIcon = Icons.AutoMirrored.Filled.FilledList,
        unselectedIcon = Icons.AutoMirrored.Outlined.OutlinedList,
    ),
    ASK(
        route = AskRoute,
        labelRes = R.string.nav_ask,
        selectedIcon = Icons.AutoMirrored.Filled.FilledSend,
        unselectedIcon = Icons.AutoMirrored.Outlined.OutlinedSend,
    ),
    BUDGETS(
        route = BudgetsRoute,
        labelRes = R.string.nav_budgets,
        selectedIcon = Icons.Filled.FilledDateRange,
        unselectedIcon = Icons.Outlined.OutlinedDateRange,
    ),
}
