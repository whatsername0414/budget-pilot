package com.budgetpilot.feature.home.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.budgetpilot.core.designsystem.components.AmountText
import com.budgetpilot.core.designsystem.components.AppCard
import com.budgetpilot.core.designsystem.components.AppTopBar
import com.budgetpilot.core.designsystem.components.BudgetProgressBar
import com.budgetpilot.core.designsystem.components.EmptyState
import com.budgetpilot.core.designsystem.components.ErrorState
import com.budgetpilot.core.designsystem.components.LoadingSkeleton
import com.budgetpilot.core.designsystem.components.SectionHeader
import com.budgetpilot.core.designsystem.theme.BudgetPilotTheme
import com.budgetpilot.core.designsystem.theme.Spacing
import com.budgetpilot.core.designsystem.theme.categoryColor
import com.budgetpilot.core.domain.money.Money
import com.budgetpilot.core.presentation.ObserveAsEvents
import com.budgetpilot.feature.home.presentation.R
import com.budgetpilot.feature.home.presentation.components.HomeExpenseRow
import com.budgetpilot.feature.home.presentation.model.HomeBudgetUi
import com.budgetpilot.feature.home.presentation.model.HomeCategoryUi
import com.budgetpilot.feature.home.presentation.model.HomeExpenseUi
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeScreen(
    onSeeAllExpenses: () -> Unit,
    onSeeBudgets: () -> Unit,
    onAddExpense: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            HomeEvent.NavigateToExpenseList -> onSeeAllExpenses()
            HomeEvent.NavigateToBudgets -> onSeeBudgets()
            HomeEvent.NavigateToAddExpense -> onAddExpense()
        }
    }

    HomeContent(state = state, onAction = viewModel::onAction, modifier = modifier)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeContent(
    state: HomeState,
    onAction: (HomeAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = { AppTopBar(title = "Budget Pilot") },
    ) { innerPadding ->
        when {
            state.isLoading -> HomeLoadingSkeleton(modifier = Modifier.padding(innerPadding))
            state.error != null ->
                ErrorState(
                    message = state.error.asString(),
                    onRetry = { onAction(HomeAction.OnRetryClick) },
                    modifier = Modifier.padding(innerPadding),
                )
            else -> HomeLoadedContent(state = state, onAction = onAction, modifier = Modifier.padding(innerPadding))
        }
    }
}

@Composable
private fun HomeLoadingSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(Spacing.medium),
        verticalArrangement = Arrangement.spacedBy(Spacing.medium),
    ) {
        LoadingSkeleton(modifier = Modifier.fillMaxWidth().height(160.dp))
        LoadingSkeleton(modifier = Modifier.fillMaxWidth().height(120.dp))
        LoadingSkeleton(modifier = Modifier.fillMaxWidth().height(120.dp))
    }
}

@Composable
private fun HomeLoadedContent(
    state: HomeState,
    onAction: (HomeAction) -> Unit,
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
        HomeHeroCard(state = state)

        HomeInsightSlot()

        if (state.isEmpty) {
            EmptyState(
                icon = Icons.Filled.Info,
                title = "No expenses yet",
                description = "Start tracking your spending by adding your first expense.",
                actionLabel = "Add expense",
                onAction = { onAction(HomeAction.OnAddExpenseClick) },
            )
        } else {
            SectionHeader(
                title = "Top categories",
                action = { TextButton(onClick = { onAction(HomeAction.OnSeeBudgetsClick) }) { Text("Charts") } },
            )
            AppCard(modifier = Modifier.fillMaxWidth()) {
                state.topCategories.forEach { category -> TopCategoryRow(category) }
            }

            if (state.worstBudgets.isNotEmpty()) {
                SectionHeader(
                    title = "Budgets",
                    action = { TextButton(onClick = { onAction(HomeAction.OnSeeBudgetsClick) }) { Text("See all") } },
                )
                AppCard(modifier = Modifier.fillMaxWidth()) {
                    state.worstBudgets.forEachIndexed { index, budget ->
                        BudgetProgressBar(spent = budget.spent, budget = budget.budget, label = budget.name)
                        if (index != state.worstBudgets.lastIndex) Spacer(Modifier.height(Spacing.medium))
                    }
                }
            }

            SectionHeader(
                title = "Recent expenses",
                action = { TextButton(onClick = { onAction(HomeAction.OnSeeAllExpensesClick) }) { Text("See all") } },
            )
            AppCard(modifier = Modifier.fillMaxWidth()) {
                state.recentExpenses.forEachIndexed { index, expense ->
                    HomeExpenseRow(expense = expense)
                    if (index != state.recentExpenses.lastIndex) {
                        HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeHeroCard(
    state: HomeState,
    modifier: Modifier = Modifier,
) {
    AppCard(modifier = modifier.fillMaxWidth()) {
        Text(
            text = state.monthEyebrow,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(Spacing.extraSmall))
        AmountText(amount = state.totalSpent, style = MaterialTheme.typography.displaySmall)
        Spacer(Modifier.height(Spacing.extraSmall))
        Row {
            Text(
                text = "of ",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            AmountText(
                amount = state.totalBudgeted,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = " budgeted · ${state.daysLeftInMonth} days left",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.height(Spacing.small))
        BudgetProgressBar(spent = state.totalSpent, budget = state.totalBudgeted, showRemaining = true)
    }
}

/** Reserved for the Phase-5 proactive-insight card (PLAN.md §6 Phase 5); intentionally empty until then. */
@Composable
private fun HomeInsightSlot(modifier: Modifier = Modifier) {
    Box(modifier = modifier)
}

@Composable
private fun TopCategoryRow(
    category: HomeCategoryUi,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = Spacing.extraSmall),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.small),
    ) {
        Text(
            text = category.name,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.width(96.dp),
        )
        Box(modifier = Modifier.weight(1f).height(20.dp)) {
            Surface(
                color = categoryColor(category.colorKey),
                shape = RoundedCornerShape(4.dp),
                modifier =
                    Modifier
                        .fillMaxWidth(category.fraction.coerceIn(0f, 1f))
                        .fillMaxHeight(),
            ) {}
        }
        AmountText(amount = category.amount, style = MaterialTheme.typography.labelMedium)
    }
}

@PreviewLightDark
@Composable
private fun HomeScreenPreview() {
    BudgetPilotTheme {
        HomeContent(
            state =
                HomeState(
                    totalSpent = Money.fromPesos("18437.50"),
                    totalBudgeted = Money.fromPesos("30000.00"),
                    daysLeftInMonth = 17,
                    topCategories =
                        listOf(
                            HomeCategoryUi(1, "Food", "food", Money.fromPesos("8200.00"), 1f),
                            HomeCategoryUi(2, "Transport", "transport", Money.fromPesos("4100.00"), 0.5f),
                            HomeCategoryUi(3, "Bills", "bills", Money.fromPesos("2050.00"), 0.25f),
                        ),
                    worstBudgets =
                        listOf(
                            HomeBudgetUi(1, "Food", Money.fromPesos("8200.00"), Money.fromPesos("9000.00")),
                            HomeBudgetUi(2, "Transport", Money.fromPesos("4100.00"), Money.fromPesos("6000.00")),
                        ),
                    recentExpenses =
                        listOf(
                            HomeExpenseUi(
                                id = 1,
                                merchant = "Jollibee SM North",
                                categoryName = "Food",
                                categoryIconKey = "restaurant",
                                categoryColorKey = "food",
                                amount = Money.fromPesos("249.00"),
                                formattedTime = "12:34 PM",
                                source = com.budgetpilot.core.domain.model.ExpenseSource.RECEIPT,
                            ),
                        ),
                    isLoading = false,
                    isEmpty = false,
                ),
            onAction = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun HomeScreenEmptyPreview() {
    BudgetPilotTheme {
        HomeContent(
            state = HomeState(isLoading = false, isEmpty = true),
            onAction = {},
        )
    }
}
