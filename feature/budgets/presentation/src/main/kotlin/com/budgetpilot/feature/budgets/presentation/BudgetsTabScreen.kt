package com.budgetpilot.feature.budgets.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.budgetpilot.core.designsystem.components.AppTopBar
import com.budgetpilot.feature.budgets.presentation.charts.ChartsScreen
import com.budgetpilot.feature.budgets.presentation.main.BudgetListScreen

// This screen owns the single AppTopBar ("Budgets") above the tab row; the two
// tabs (BudgetListScreen/ChartsScreen) render their content only, no top bar of
// their own, so the title doesn't change/duplicate when switching tabs.
private enum class BudgetsTab {
    BUDGETS,
    CHARTS,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetsTabScreen(modifier: Modifier = Modifier) {
    var selectedTab by rememberSaveable { mutableStateOf(BudgetsTab.BUDGETS) }

    Scaffold(
        modifier = modifier,
        topBar = { AppTopBar(title = stringResource(R.string.budgets_top_bar_title)) },
    ) { innerPadding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
        ) {
            SecondaryTabRow(
                selectedTabIndex = selectedTab.ordinal,
                containerColor = MaterialTheme.colorScheme.background,
                indicator = {
                    Box(
                        modifier =
                            Modifier
                                .tabIndicatorOffset(selectedTab.ordinal, matchContentSize = false)
                                .height(3.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(
                            modifier =
                                Modifier
                                    .fillMaxWidth(fraction = 0.5f)
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                                    .background(MaterialTheme.colorScheme.primary),
                        )
                    }
                },
            ) {
                Tab(
                    selected = selectedTab == BudgetsTab.BUDGETS,
                    onClick = { selectedTab = BudgetsTab.BUDGETS },
                    text = { Text(stringResource(R.string.tab_budgets)) },
                )
                Tab(
                    selected = selectedTab == BudgetsTab.CHARTS,
                    onClick = { selectedTab = BudgetsTab.CHARTS },
                    text = { Text(stringResource(R.string.tab_charts)) },
                )
            }
            when (selectedTab) {
                BudgetsTab.BUDGETS -> BudgetListScreen(modifier = Modifier.weight(1f))
                BudgetsTab.CHARTS -> ChartsScreen(modifier = Modifier.weight(1f))
            }
        }
    }
}
