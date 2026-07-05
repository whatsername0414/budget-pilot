package com.budgetpilot.feature.budgets.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.budgetpilot.feature.budgets.presentation.charts.ChartsRoot
import com.budgetpilot.feature.budgets.presentation.main.BudgetListRoot

// DESIGN-SPEC.md §6 shows one shared AppTopBar/MonthSelector above the tab row.
// BudgetListScreen/ChartsScreen already ship their own (built in P1.5/P1.6), so
// each tab keeps its own independent top bar and month state rather than a
// deeper shared-state refactor out of scope for this nav-wiring step.
private enum class BudgetsTab {
    BUDGETS,
    CHARTS,
}

@Composable
fun BudgetsTabRoot(modifier: Modifier = Modifier) {
    var selectedTab by rememberSaveable { mutableStateOf(BudgetsTab.BUDGETS) }

    Column(modifier = modifier.fillMaxSize()) {
        SecondaryTabRow(selectedTabIndex = selectedTab.ordinal) {
            Tab(
                selected = selectedTab == BudgetsTab.BUDGETS,
                onClick = { selectedTab = BudgetsTab.BUDGETS },
                text = { Text("Budgets") },
            )
            Tab(
                selected = selectedTab == BudgetsTab.CHARTS,
                onClick = { selectedTab = BudgetsTab.CHARTS },
                text = { Text("Charts") },
            )
        }
        when (selectedTab) {
            BudgetsTab.BUDGETS -> BudgetListRoot(modifier = Modifier.weight(1f))
            BudgetsTab.CHARTS -> ChartsRoot(modifier = Modifier.weight(1f))
        }
    }
}
