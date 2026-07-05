package com.budgetpilot.feature.budgets.presentation.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.budgetpilot.core.designsystem.components.AmountText
import com.budgetpilot.core.designsystem.components.AppCard
import com.budgetpilot.core.designsystem.components.AppTopBar
import com.budgetpilot.core.designsystem.components.BudgetProgressBar
import com.budgetpilot.core.designsystem.components.EmptyState
import com.budgetpilot.core.designsystem.components.ErrorState
import com.budgetpilot.core.designsystem.components.LoadingSkeleton
import com.budgetpilot.core.designsystem.icons.categoryIcon
import com.budgetpilot.core.designsystem.theme.Spacing
import com.budgetpilot.core.designsystem.theme.categoryColor
import com.budgetpilot.core.domain.money.Money
import com.budgetpilot.core.presentation.ObserveAsEvents
import com.budgetpilot.feature.budgets.presentation.editor.BudgetEditorSheet
import com.budgetpilot.feature.budgets.presentation.main.components.MonthSelector
import com.budgetpilot.feature.budgets.presentation.main.components.dashedBorder
import com.budgetpilot.feature.budgets.presentation.main.model.BudgetCategoryUi
import com.budgetpilot.feature.budgets.presentation.main.model.UnbudgetedCategoryUi
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun BudgetListScreen(
    modifier: Modifier = Modifier,
    viewModel: BudgetListViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is BudgetListEvent.ShowSnackbar -> scope.launch { snackbarHostState.showSnackbar(event.message) }
            is BudgetListEvent.ShowError ->
                scope.launch { snackbarHostState.showSnackbar(event.message.asString(context)) }
        }
    }

    BudgetListContent(
        state = state,
        onAction = viewModel::onAction,
        modifier = modifier,
        snackbarHostState = snackbarHostState,
    )

    val editingCategoryId = state.editingCategoryId
    if (editingCategoryId != null) {
        BudgetEditorSheet(
            categoryId = editingCategoryId,
            month = state.month,
            onDismiss = { confirmationMessage ->
                viewModel.onAction(BudgetListAction.OnDismissEditor)
                if (confirmationMessage != null) {
                    scope.launch { snackbarHostState.showSnackbar(confirmationMessage) }
                }
            },
            onError = { message -> scope.launch { snackbarHostState.showSnackbar(message.asString(context)) } },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetListContent(
    state: BudgetListState,
    onAction: (BudgetListAction) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    Scaffold(
        modifier = modifier,
        topBar = { AppTopBar(title = "Budgets") },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
        ) {
            MonthSelector(
                monthLabel = state.monthLabel,
                onPreviousClick = { onAction(BudgetListAction.OnPreviousMonthClick) },
                onNextClick = { onAction(BudgetListAction.OnNextMonthClick) },
                isNextEnabled = state.canGoToNextMonth,
                modifier = Modifier.padding(horizontal = Spacing.medium, vertical = Spacing.small),
            )

            when {
                state.isLoading -> LoadingBudgetsSkeleton()
                state.error != null && state.budgetedCategories.isEmpty() && state.unbudgetedCategories.isEmpty() ->
                    ErrorState(
                        message = state.error.asString(),
                        onRetry = { onAction(BudgetListAction.OnRetryClick) },
                    )
                else ->
                    BudgetListLoadedContent(
                        state = state,
                        onAction = onAction,
                    )
            }
        }
    }
}

@Composable
private fun LoadingBudgetsSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(Spacing.medium),
        verticalArrangement = Arrangement.spacedBy(Spacing.small),
    ) {
        repeat(3) {
            LoadingSkeleton(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(88.dp),
            )
        }
    }
}

@Composable
private fun BudgetListLoadedContent(
    state: BudgetListState,
    onAction: (BudgetListAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        BudgetSummaryCard(
            budgeted = state.totalBudgeted,
            spent = state.totalSpent,
            remaining = state.totalRemaining,
            modifier = Modifier.padding(horizontal = Spacing.medium),
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = Spacing.medium, vertical = Spacing.small),
            verticalArrangement = Arrangement.spacedBy(Spacing.small),
        ) {
            if (state.hasNoBudgets) {
                item {
                    EmptyState(
                        icon = Icons.Filled.Info,
                        title = "Set your first budget",
                        description = "Tap a category below to add one.",
                    )
                }
            } else {
                items(items = state.budgetedCategories, key = { it.categoryId }) { category ->
                    BudgetCategoryCard(
                        category = category,
                        isReadOnly = state.isReadOnly,
                        onEditClick = { onAction(BudgetListAction.OnEditBudgetClick(category.categoryId)) },
                    )
                }
            }
            items(items = state.unbudgetedCategories, key = { it.categoryId }) { category ->
                UnbudgetedCategoryRow(
                    category = category,
                    isReadOnly = state.isReadOnly,
                    onSetBudgetClick = { onAction(BudgetListAction.OnEditBudgetClick(category.categoryId)) },
                )
            }
        }
    }
}

@Composable
private fun BudgetSummaryCard(
    budgeted: Money,
    spent: Money,
    remaining: Money,
    modifier: Modifier = Modifier,
) {
    AppCard(modifier = modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            SummaryColumn(label = "Budgeted", amount = budgeted)
            SummaryColumn(label = "Spent", amount = spent)
            SummaryColumn(
                label = "Left",
                amount = remaining,
                color =
                    if (remaining < Money.ZERO) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.tertiary
                    },
            )
        }
    }
}

@Composable
private fun SummaryColumn(
    label: String,
    amount: Money,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
) {
    Column(modifier = modifier) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        AmountText(amount = amount, style = MaterialTheme.typography.titleMedium, color = color)
    }
}

@Composable
private fun BudgetCategoryCard(
    category: BudgetCategoryUi,
    isReadOnly: Boolean,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AppCard(modifier = modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = categoryIcon(category.iconKey),
                contentDescription = null,
                tint = categoryColor(category.colorKey),
            )
            Spacer(Modifier.width(Spacing.small))
            Text(
                text = category.name,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f),
            )
            if (!isReadOnly) {
                IconButton(onClick = onEditClick) {
                    Icon(imageVector = Icons.Filled.Edit, contentDescription = "Edit ${category.name} budget")
                }
            }
        }
        Spacer(Modifier.height(Spacing.small))
        BudgetProgressBar(spent = category.spent, budget = category.budget)
    }
}

@Composable
private fun UnbudgetedCategoryRow(
    category: UnbudgetedCategoryUi,
    isReadOnly: Boolean,
    onSetBudgetClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .dashedBorder(color = MaterialTheme.colorScheme.outline)
                .padding(Spacing.medium),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = categoryIcon(category.iconKey),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.width(Spacing.small))
        Text(
            text = "${category.name} — no budget yet",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        if (!isReadOnly) {
            TextButton(onClick = onSetBudgetClick) {
                Text("Set budget")
            }
        }
    }
}
