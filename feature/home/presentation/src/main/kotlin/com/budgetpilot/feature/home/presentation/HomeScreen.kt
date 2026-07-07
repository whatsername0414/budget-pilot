package com.budgetpilot.feature.home.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
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
import com.budgetpilot.core.designsystem.icons.StateIcons
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
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
    insightSlot: @Composable () -> Unit = {},
    viewModel: HomeViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            HomeEvent.NavigateToExpenseList -> onSeeAllExpenses()
            HomeEvent.NavigateToBudgets -> onSeeBudgets()
            HomeEvent.NavigateToAddExpense -> onAddExpense()
            HomeEvent.NavigateToSettings -> onOpenSettings()
        }
    }

    HomeContent(state = state, onAction = viewModel::onAction, insightSlot = insightSlot, modifier = modifier)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeContent(
    state: HomeState,
    onAction: (HomeAction) -> Unit,
    modifier: Modifier = Modifier,
    insightSlot: @Composable () -> Unit = {},
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            AppTopBar(
                title = stringResource(R.string.home_top_bar_title),
                actions = {
                    IconButton(onClick = { onAction(HomeAction.OnSettingsClick) }) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = stringResource(R.string.cd_open_settings),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        when {
            state.isLoading -> HomeLoadingSkeleton(modifier = Modifier.padding(innerPadding))
            state.error != null ->
                ErrorState(
                    message = state.error.asString(),
                    onRetry = { onAction(HomeAction.OnRetryClick) },
                    modifier = Modifier.padding(innerPadding),
                )
            else ->
                HomeLoadedContent(
                    state = state,
                    onAction = onAction,
                    insightSlot = insightSlot,
                    modifier = Modifier.padding(innerPadding),
                )
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
    insightSlot: @Composable () -> Unit = {},
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

        insightSlot()

        if (state.isEmpty) {
            EmptyState(
                icon = StateIcons.Receipt,
                title = stringResource(R.string.home_empty_title),
                description = stringResource(R.string.home_empty_description),
                actionLabel = stringResource(R.string.action_add_expense),
                onAction = { onAction(HomeAction.OnAddExpenseClick) },
            )
        } else {
            SectionHeader(
                title = stringResource(R.string.home_section_top_categories),
                action = {
                    TextButton(onClick = { onAction(HomeAction.OnSeeBudgetsClick) }) {
                        Text(stringResource(R.string.action_charts))
                    }
                },
            )
            AppCard(modifier = Modifier.fillMaxWidth()) {
                state.topCategories.forEachIndexed { index, category ->
                    TopCategoryRow(category)
                    if (index != state.topCategories.lastIndex) Spacer(Modifier.height(TopCategoryRowGap))
                }
            }

            if (state.worstBudgets.isNotEmpty()) {
                SectionHeader(
                    title = stringResource(R.string.home_section_budgets),
                    action = {
                        TextButton(onClick = { onAction(HomeAction.OnSeeBudgetsClick) }) {
                            Text(stringResource(R.string.action_see_all))
                        }
                    },
                )
                AppCard(modifier = Modifier.fillMaxWidth()) {
                    state.worstBudgets.forEachIndexed { index, budget ->
                        BudgetProgressBar(spent = budget.spent, budget = budget.budget, label = budget.name)
                        if (index != state.worstBudgets.lastIndex) Spacer(Modifier.height(Spacing.medium))
                    }
                }
            }

            SectionHeader(
                title = stringResource(R.string.home_section_recent_expenses),
                action = {
                    TextButton(onClick = { onAction(HomeAction.OnSeeAllExpensesClick) }) {
                        Text(stringResource(R.string.action_see_all))
                    }
                },
            )
            AppCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = Spacing.medium, vertical = RecentExpensesCardVerticalPadding),
            ) {
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
        Spacer(Modifier.height(Spacing.small))
        AmountText(amount = state.totalSpent, style = MaterialTheme.typography.displaySmall)
        Spacer(Modifier.height(Spacing.small))
        Row {
            Text(
                text = stringResource(R.string.home_hero_of),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            AmountText(
                amount = state.totalBudgeted,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(R.string.home_hero_budgeted_days_left, state.daysLeftInMonth),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.height(Spacing.small))
        BudgetProgressBar(spent = state.totalSpent, budget = state.totalBudgeted, showRemaining = true)
    }
}

@Composable
private fun TopCategoryRow(
    category: HomeCategoryUi,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(TopCategoryRowItemGap),
    ) {
        Text(
            text = category.name,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.width(96.dp),
        )
        Box(
            modifier =
                Modifier
                    .weight(1f)
                    .height(20.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.08f)),
        ) {
            Surface(
                color = categoryColor(category.colorKey),
                shape = RoundedCornerShape(4.dp),
                modifier =
                    Modifier
                        .fillMaxWidth(category.fraction.coerceIn(0f, 1f))
                        .fillMaxHeight(),
            ) {}
        }
        AmountText(
            amount = category.amount,
            style = MaterialTheme.typography.labelMedium.copy(textAlign = TextAlign.End),
            modifier = Modifier.width(TopCategoryValueWidth),
        )
    }
}

private val TopCategoryRowGap = 12.dp
private val TopCategoryRowItemGap = 10.dp
private val TopCategoryValueWidth = 74.dp
private val RecentExpensesCardVerticalPadding = 4.dp

@Preview
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

@Preview
@Composable
private fun HomeScreenEmptyPreview() {
    BudgetPilotTheme {
        HomeContent(
            state = HomeState(isLoading = false, isEmpty = true),
            onAction = {},
        )
    }
}
