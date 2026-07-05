package com.budgetpilot.feature.expenses.presentation.main

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.budgetpilot.core.designsystem.components.AmountText
import com.budgetpilot.core.designsystem.components.AppCard
import com.budgetpilot.core.designsystem.components.AppTopBar
import com.budgetpilot.core.designsystem.components.EmptyState
import com.budgetpilot.core.designsystem.components.LoadingSkeleton
import com.budgetpilot.core.designsystem.theme.BudgetPilotTheme
import com.budgetpilot.core.designsystem.theme.Spacing
import com.budgetpilot.core.domain.model.Category
import com.budgetpilot.core.domain.model.ExpenseSource
import com.budgetpilot.core.domain.money.Money
import com.budgetpilot.core.presentation.ObserveAsEvents
import com.budgetpilot.feature.expenses.presentation.main.components.DateRangeFilterSheet
import com.budgetpilot.feature.expenses.presentation.main.components.ExpenseFilterChipRow
import com.budgetpilot.feature.expenses.presentation.main.components.ExpenseRow
import com.budgetpilot.feature.expenses.presentation.main.components.ExpenseSearchField
import com.budgetpilot.feature.expenses.presentation.main.model.DateRangePreset
import com.budgetpilot.feature.expenses.presentation.main.model.ExpenseDayGroupUi
import com.budgetpilot.feature.expenses.presentation.main.model.ExpenseUi
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDate

@Composable
fun ExpenseListRoot(
    onNavigateToExpenseEditor: (Long?) -> Unit,
    modifier: Modifier = Modifier,
    confirmationMessage: String? = null,
    onConfirmationMessageDismiss: () -> Unit = {},
    viewModel: ExpenseListViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val currentOnConfirmationMessageDismiss by rememberUpdatedState(onConfirmationMessageDismiss)

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is ExpenseListEvent.NavigateToExpenseEditor -> onNavigateToExpenseEditor(event.expenseId)
            is ExpenseListEvent.ShowError -> {
                scope.launch {
                    snackbarHostState.showSnackbar(message = event.message.asString(context))
                }
            }
            is ExpenseListEvent.ShowUndoDeleteSnackbar -> {
                scope.launch {
                    val result =
                        snackbarHostState.showSnackbar(
                            message = "${event.merchant} deleted",
                            actionLabel = "Undo",
                            duration = SnackbarDuration.Short,
                        )
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.onAction(ExpenseListAction.OnUndoDeleteClick)
                    }
                }
            }
        }
    }

    LaunchedEffect(confirmationMessage) {
        if (confirmationMessage != null) {
            snackbarHostState.showSnackbar(confirmationMessage)
            currentOnConfirmationMessageDismiss()
        }
    }

    ExpenseListScreen(
        state = state,
        onAction = viewModel::onAction,
        modifier = modifier,
        snackbarHostState = snackbarHostState,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseListScreen(
    state: ExpenseListState,
    onAction: (ExpenseListAction) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    Scaffold(
        modifier = modifier,
        topBar = { AppTopBar(title = "History") },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
        ) {
            ExpenseSearchField(
                query = state.searchQuery,
                onQueryChange = { onAction(ExpenseListAction.OnSearchQueryChange(it)) },
                modifier = Modifier.padding(horizontal = Spacing.medium, vertical = Spacing.small),
            )
            ExpenseFilterChipRow(
                categories = state.categories,
                selectedCategoryId = state.selectedCategoryId,
                hasDateFilterActive = state.dateRangePreset != DateRangePreset.THIS_MONTH,
                onFiltersClick = { onAction(ExpenseListAction.OnFiltersClick) },
                onCategorySelect = { onAction(ExpenseListAction.OnCategoryFilterSelect(it)) },
                modifier = Modifier.padding(top = Spacing.small),
            )

            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    state.isLoading -> LoadingListSkeleton(modifier = Modifier.align(Alignment.TopCenter))
                    state.isEmpty && state.hasActiveFilter ->
                        EmptyState(
                            icon = Icons.Filled.Info,
                            title = "No matches for that",
                            description = "",
                            modifier = Modifier.align(Alignment.Center),
                        )
                    state.isEmpty ->
                        EmptyState(
                            icon = Icons.Filled.Info,
                            title = "No expenses yet",
                            description = "Add your first expense to start tracking your budget.",
                            actionLabel = "Add expense",
                            onAction = { onAction(ExpenseListAction.OnAddExpenseClick) },
                            modifier = Modifier.align(Alignment.Center),
                        )
                    else ->
                        ExpenseDayList(
                            dayGroups = state.dayGroups,
                            onExpenseClick = { onAction(ExpenseListAction.OnExpenseClick(it)) },
                            onExpenseDelete = { onAction(ExpenseListAction.OnDeleteExpense(it)) },
                        )
                }
            }
        }
    }

    if (state.isFilterSheetVisible) {
        DateRangeFilterSheet(
            selectedPreset = state.dateRangePreset,
            onPresetSelect = { onAction(ExpenseListAction.OnDateRangePresetSelect(it)) },
            onDismiss = { onAction(ExpenseListAction.OnDismissFilterSheet) },
        )
    }
}

@Composable
private fun LoadingListSkeleton(modifier: Modifier = Modifier) {
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
                        .height(60.dp),
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ExpenseDayList(
    dayGroups: List<ExpenseDayGroupUi>,
    onExpenseClick: (Long) -> Unit,
    onExpenseDelete: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = Spacing.medium, vertical = Spacing.small),
        verticalArrangement = Arrangement.spacedBy(Spacing.small),
    ) {
        dayGroups.forEach { dayGroup ->
            stickyHeader(key = "header_${dayGroup.date}") {
                DayGroupHeader(dayGroup = dayGroup)
            }
            item(key = "card_${dayGroup.date}") {
                AppCard {
                    dayGroup.expenses.forEach { expense ->
                        SwipeableExpenseRow(
                            expense = expense,
                            onClick = { onExpenseClick(expense.id) },
                            onDelete = { onExpenseDelete(expense.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DayGroupHeader(
    dayGroup: ExpenseDayGroupUi,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(vertical = Spacing.extraSmall),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = dayGroup.dateLabel, style = MaterialTheme.typography.titleSmall)
        AmountText(
            amount = dayGroup.totalAmount,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableExpenseRow(
    expense: ExpenseUi,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dismissState =
        rememberSwipeToDismissBoxState(
            confirmValueChange = { value ->
                if (value != SwipeToDismissBoxValue.Settled) {
                    onDelete()
                }
                true
            },
        )

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier,
        backgroundContent = {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.error)
                        .padding(horizontal = Spacing.medium),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Delete expense",
                    tint = MaterialTheme.colorScheme.onError,
                )
            }
        },
    ) {
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
            ExpenseRow(
                expense = expense,
                onClick = onClick,
                modifier = Modifier.padding(horizontal = Spacing.medium),
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun ExpenseListScreenPreview() {
    BudgetPilotTheme {
        ExpenseListScreen(
            state =
                ExpenseListState(
                    isLoading = false,
                    categories =
                        listOf(
                            Category(1, "Food", "restaurant", "food", true),
                            Category(2, "Transport", "directions_bus", "transport", true),
                        ),
                    dayGroups =
                        listOf(
                            ExpenseDayGroupUi(
                                date = LocalDate.now(),
                                dateLabel = "Today",
                                totalAmount = Money.fromPesos("269.00"),
                                expenses =
                                    listOf(
                                        ExpenseUi(
                                            id = 1,
                                            merchant = "Jollibee SM North",
                                            categoryName = "Food",
                                            categoryIconKey = "restaurant",
                                            categoryColorKey = "food",
                                            amount = Money.fromPesos("249.00"),
                                            formattedTime = "12:34 PM",
                                            source = ExpenseSource.RECEIPT,
                                        ),
                                        ExpenseUi(
                                            id = 2,
                                            merchant = "Cash — parking",
                                            categoryName = "Transport",
                                            categoryIconKey = "directions_bus",
                                            categoryColorKey = "transport",
                                            amount = Money.fromPesos("20.00"),
                                            formattedTime = "5:10 PM",
                                            source = ExpenseSource.MANUAL,
                                        ),
                                    ),
                            ),
                        ),
                ),
            onAction = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun ExpenseListScreenEmptyPreview() {
    BudgetPilotTheme {
        ExpenseListScreen(
            state = ExpenseListState(isLoading = false),
            onAction = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun ExpenseListScreenLoadingPreview() {
    BudgetPilotTheme {
        ExpenseListScreen(
            state = ExpenseListState(isLoading = true),
            onAction = {},
        )
    }
}
