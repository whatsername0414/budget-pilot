package com.budgetpilot.feature.budgets.presentation.charts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.budgetpilot.core.designsystem.components.AppCard
import com.budgetpilot.core.designsystem.components.AppTopBar
import com.budgetpilot.core.designsystem.components.EmptyState
import com.budgetpilot.core.designsystem.components.ErrorState
import com.budgetpilot.core.designsystem.components.LoadingSkeleton
import com.budgetpilot.core.designsystem.components.SectionHeader
import com.budgetpilot.core.designsystem.theme.BudgetPilotTheme
import com.budgetpilot.core.designsystem.theme.Spacing
import com.budgetpilot.core.domain.money.Money
import com.budgetpilot.feature.budgets.presentation.charts.components.CategorySpendChart
import com.budgetpilot.feature.budgets.presentation.charts.components.MonthlyTrendChart
import com.budgetpilot.feature.budgets.presentation.charts.model.CategorySpendUi
import com.budgetpilot.feature.budgets.presentation.charts.model.MonthlyTrendPointUi
import com.budgetpilot.feature.budgets.presentation.main.components.MonthSelector
import org.koin.androidx.compose.koinViewModel
import java.time.YearMonth

@Composable
fun ChartsScreen(
    modifier: Modifier = Modifier,
    viewModel: ChartsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ChartsContent(state = state, onAction = viewModel::onAction, modifier = modifier)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChartsContent(
    state: ChartsState,
    onAction: (ChartsAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = { AppTopBar(title = "Charts") },
    ) { innerPadding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
        ) {
            MonthSelector(
                monthLabel = state.monthLabel,
                onPreviousClick = { onAction(ChartsAction.OnPreviousMonthClick) },
                onNextClick = { onAction(ChartsAction.OnNextMonthClick) },
                isNextEnabled = state.canGoToNextMonth,
                modifier = Modifier.padding(horizontal = Spacing.medium, vertical = Spacing.small),
            )

            when {
                state.isLoading -> LoadingChartsSkeleton()
                state.error != null ->
                    ErrorState(
                        message = state.error.asString(),
                        onRetry = { onAction(ChartsAction.OnRetryClick) },
                    )
                else -> ChartsLoadedContent(state = state)
            }
        }
    }
}

@Composable
private fun LoadingChartsSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(Spacing.medium),
        verticalArrangement = Arrangement.spacedBy(Spacing.medium),
    ) {
        LoadingSkeleton(modifier = Modifier.fillMaxWidth().height(160.dp))
        LoadingSkeleton(modifier = Modifier.fillMaxWidth().height(220.dp))
    }
}

@Composable
private fun ChartsLoadedContent(
    state: ChartsState,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(Spacing.medium),
        verticalArrangement = Arrangement.spacedBy(Spacing.medium),
    ) {
        AppCard(modifier = Modifier.fillMaxWidth()) {
            SectionHeader(title = "Spending by category")
            if (state.hasCategorySpend) {
                CategorySpendChart(
                    categorySpend = state.categorySpend,
                    modifier = Modifier.padding(top = Spacing.small),
                )
            } else {
                EmptyState(
                    icon = Icons.Filled.Info,
                    title = "No expenses yet",
                    description = "Add your first expense to start tracking your budget.",
                )
            }
        }

        AppCard(modifier = Modifier.fillMaxWidth()) {
            SectionHeader(title = "6-month trend")
            if (state.hasEnoughTrendData) {
                MonthlyTrendChart(
                    points = state.monthlyTrend,
                    modifier = Modifier.padding(top = Spacing.small),
                )
                Text(
                    text = state.trendCaption,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = Spacing.extraSmall),
                )
            } else {
                EmptyState(
                    icon = Icons.Filled.Info,
                    title = "Not enough data yet",
                    description = "The trend chart needs at least two months of expenses.",
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun ChartsScreenPreview() {
    BudgetPilotTheme {
        ChartsContent(
            state =
                ChartsState(
                    isLoading = false,
                    categorySpend =
                        listOf(
                            CategorySpendUi(1, "Food", "food", Money.fromPesos("4200.00"), 1f),
                            CategorySpendUi(2, "Transport", "transport", Money.fromPesos("2100.00"), 0.5f),
                            CategorySpendUi(3, "Bills", "bills", Money.fromPesos("950.00"), 0.226f),
                        ),
                    monthlyTrend =
                        listOf(
                            MonthlyTrendPointUi(YearMonth.now().minusMonths(5), "Feb", Money.fromPesos("12000.00")),
                            MonthlyTrendPointUi(YearMonth.now().minusMonths(4), "Mar", Money.fromPesos("15500.00")),
                            MonthlyTrendPointUi(YearMonth.now().minusMonths(3), "Apr", Money.fromPesos("9800.00")),
                            MonthlyTrendPointUi(YearMonth.now().minusMonths(2), "May", Money.fromPesos("24310.00")),
                            MonthlyTrendPointUi(YearMonth.now().minusMonths(1), "Jun", Money.fromPesos("18200.00")),
                            MonthlyTrendPointUi(YearMonth.now(), "Jul", Money.fromPesos("6100.00")),
                        ),
                ),
            onAction = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun ChartsScreenEmptyPreview() {
    BudgetPilotTheme {
        ChartsContent(state = ChartsState(isLoading = false), onAction = {})
    }
}

@PreviewLightDark
@Composable
private fun ChartsScreenLoadingPreview() {
    BudgetPilotTheme {
        ChartsContent(state = ChartsState(isLoading = true), onAction = {})
    }
}
