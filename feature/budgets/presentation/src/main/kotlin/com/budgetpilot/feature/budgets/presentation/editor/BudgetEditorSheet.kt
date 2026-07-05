package com.budgetpilot.feature.budgets.presentation.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.budgetpilot.core.designsystem.components.AmountText
import com.budgetpilot.core.designsystem.icons.categoryIcon
import com.budgetpilot.core.designsystem.theme.BudgetPilotTheme
import com.budgetpilot.core.designsystem.theme.Spacing
import com.budgetpilot.core.designsystem.theme.categoryColor
import com.budgetpilot.core.domain.money.Money
import com.budgetpilot.core.presentation.ObserveAsEvents
import com.budgetpilot.core.presentation.UiText
import com.budgetpilot.core.presentation.money.PesoFormatter
import com.budgetpilot.feature.budgets.presentation.editor.components.AmountKeypad
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import java.time.YearMonth

private val QuickAmounts =
    listOf(
        Money.fromPesos("3000.00"),
        Money.fromPesos("6000.00"),
        Money.fromPesos("8000.00"),
        Money.fromPesos("10000.00"),
    )

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetEditorSheet(
    categoryId: Long,
    month: YearMonth,
    onDismiss: (confirmationMessage: String?) -> Unit,
    onError: (UiText) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BudgetEditorViewModel =
        koinViewModel(
            key = "budget_editor_${categoryId}_$month",
            parameters = { parametersOf(categoryId, month) },
        ),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is BudgetEditorEvent.Dismiss -> onDismiss(event.confirmationMessage)
            is BudgetEditorEvent.ShowError -> onError(event.message)
        }
    }

    ModalBottomSheet(
        onDismissRequest = { onDismiss(null) },
        modifier = modifier,
        sheetState = sheetState,
    ) {
        BudgetEditorContent(state = state, onAction = viewModel::onAction)
    }
}

@Composable
private fun BudgetEditorContent(
    state: BudgetEditorState,
    onAction: (BudgetEditorAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.medium, vertical = Spacing.small),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = categoryIcon(state.categoryIconKey),
                contentDescription = null,
                tint = categoryColor(state.categoryColorKey),
            )
            Spacer(Modifier.width(Spacing.small))
            Text(
                text = "${state.categoryName} budget / ${state.monthLabel}",
                style = MaterialTheme.typography.titleMedium,
            )
        }
        Spacer(Modifier.height(Spacing.medium))
        BudgetAmountSection(state = state)
        Spacer(Modifier.height(Spacing.medium))
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.small)) {
            QuickAmounts.forEach { amount ->
                AssistChip(
                    onClick = { onAction(BudgetEditorAction.OnQuickAmountSelect(amount)) },
                    label = { Text(PesoFormatter.format(amount)) },
                )
            }
        }
        Spacer(Modifier.height(Spacing.medium))
        AmountKeypad(onKeyPress = { onAction(BudgetEditorAction.OnAmountKeyPress(it)) })
        Spacer(Modifier.height(Spacing.medium))
        BudgetEditorActionsRow(state = state, onAction = onAction)
    }

    if (state.isRemoveConfirmVisible) {
        RemoveBudgetDialog(categoryName = state.categoryName, monthLabel = state.monthLabel, onAction = onAction)
    }
}

@Composable
private fun BudgetAmountSection(
    state: BudgetEditorState,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        AmountText(amount = state.displayAmount, style = MaterialTheme.typography.headlineMedium)
        if (state.amountError != null) {
            Text(
                text = state.amountError.asString(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
        state.lastMonthSpent?.let { spent ->
            Spacer(Modifier.height(Spacing.extraSmall))
            Text(
                text = "You spent ${PesoFormatter.format(spent)} on ${state.categoryName} last month.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun BudgetEditorActionsRow(
    state: BudgetEditorState,
    onAction: (BudgetEditorAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.small),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (state.isEditing) {
            TextButton(onClick = { onAction(BudgetEditorAction.OnRemoveClick) }) {
                Text(text = "Remove", color = MaterialTheme.colorScheme.error)
            }
        }
        Spacer(Modifier.weight(1f))
        TextButton(onClick = { onAction(BudgetEditorAction.OnDismissClick) }) {
            Text("Cancel")
        }
        Button(onClick = { onAction(BudgetEditorAction.OnSaveClick) }, enabled = !state.isSaving) {
            Text("Save")
        }
    }
}

@Composable
private fun RemoveBudgetDialog(
    categoryName: String,
    monthLabel: String,
    onAction: (BudgetEditorAction) -> Unit,
) {
    AlertDialog(
        onDismissRequest = { onAction(BudgetEditorAction.OnDismissRemoveDialog) },
        confirmButton = {
            TextButton(onClick = { onAction(BudgetEditorAction.OnConfirmRemoveClick) }) {
                Text("Remove")
            }
        },
        dismissButton = {
            TextButton(onClick = { onAction(BudgetEditorAction.OnDismissRemoveDialog) }) {
                Text("Cancel")
            }
        },
        title = { Text("Remove budget?") },
        text = { Text("This removes the $categoryName budget for $monthLabel.") },
    )
}

@PreviewLightDark
@Composable
private fun BudgetEditorContentPreview() {
    BudgetPilotTheme {
        Surface {
            BudgetEditorContent(
                state =
                    BudgetEditorState(
                        categoryName = "Food",
                        categoryIconKey = "restaurant",
                        categoryColorKey = "food",
                        monthLabel = "July 2026",
                        amountText = "6000.00",
                        lastMonthSpent = Money.fromPesos("5,872.25"),
                        isEditing = true,
                        isLoading = false,
                    ),
                onAction = {},
            )
        }
    }
}
