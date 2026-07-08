package com.budgetpilot.feature.budgets.presentation.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.budgetpilot.core.designsystem.components.LoadingSkeleton
import com.budgetpilot.core.designsystem.icons.categoryIcon
import com.budgetpilot.core.designsystem.theme.BudgetPilotTheme
import com.budgetpilot.core.designsystem.theme.Spacing
import com.budgetpilot.core.designsystem.theme.categoryColor
import com.budgetpilot.core.domain.money.Money
import com.budgetpilot.core.presentation.ObserveAsEvents
import com.budgetpilot.core.presentation.UiText
import com.budgetpilot.core.presentation.money.PesoFormatter
import com.budgetpilot.feature.budgets.presentation.R
import com.budgetpilot.feature.budgets.presentation.editor.components.AmountKeypad
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import java.time.YearMonth

private val HeaderIconSize = 40.dp
private val HeaderIconRadius = 12.dp
private val HeaderRowGap = 12.dp
private val TitleSkeletonWidth = 120.dp
private val TitleSkeletonHeight = 20.dp
private val SubtitleSkeletonWidth = 90.dp
private val SubtitleSkeletonHeight = 16.dp
private val AmountFieldSkeletonHeight = 56.dp
private val KeypadSkeletonHeight = 200.dp

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
        if (state.isLoading) {
            BudgetEditorLoadingSkeleton()
            return@Column
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(HeaderRowGap),
        ) {
            val categoryColor = categoryColor(state.categoryColorKey)
            Box(
                modifier =
                    Modifier
                        .size(HeaderIconSize)
                        .clip(RoundedCornerShape(HeaderIconRadius))
                        .background(categoryColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = categoryIcon(state.categoryIconKey),
                    contentDescription = null,
                    tint = categoryColor,
                    modifier = Modifier.size(20.dp),
                )
            }
            Column {
                Text(
                    text = stringResource(R.string.budget_editor_title, state.categoryName),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = state.monthLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Spacer(Modifier.height(Spacing.medium))
        BudgetAmountSection(state = state)
        Spacer(Modifier.height(Spacing.medium))
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.small)) {
            QuickAmounts.forEach { amount ->
                FilterChip(
                    selected = state.parsedAmount == amount,
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
private fun BudgetEditorLoadingSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.medium),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(HeaderRowGap)) {
            LoadingSkeleton(
                shape = RoundedCornerShape(HeaderIconRadius),
                modifier = Modifier.size(HeaderIconSize),
            )
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.extraSmall)) {
                LoadingSkeleton(modifier = Modifier.width(TitleSkeletonWidth).height(TitleSkeletonHeight))
                LoadingSkeleton(modifier = Modifier.width(SubtitleSkeletonWidth).height(SubtitleSkeletonHeight))
            }
        }
        LoadingSkeleton(modifier = Modifier.fillMaxWidth().height(AmountFieldSkeletonHeight))
        LoadingSkeleton(modifier = Modifier.fillMaxWidth().height(KeypadSkeletonHeight))
    }
}

@Composable
private fun BudgetAmountSection(
    state: BudgetEditorState,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = PesoFormatter.format(state.displayAmount),
            onValueChange = {},
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            singleLine = true,
            label = { Text(stringResource(R.string.budget_editor_amount_label)) },
            textStyle =
                MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontFeatureSettings = "tnum",
                ),
            isError = state.amountError != null,
            colors =
                OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.primary,
                ),
        )
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
                text =
                    stringResource(
                        R.string.budget_editor_last_month_spent,
                        PesoFormatter.format(spent),
                        state.categoryName,
                    ),
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
                Text(text = stringResource(R.string.action_remove), color = MaterialTheme.colorScheme.error)
            }
        }
        Spacer(Modifier.weight(1f))
        TextButton(onClick = { onAction(BudgetEditorAction.OnDismissClick) }) {
            Text(stringResource(R.string.action_cancel))
        }
        Button(onClick = { onAction(BudgetEditorAction.OnSaveClick) }, enabled = !state.isSaving) {
            Text(stringResource(R.string.action_save))
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
                Text(stringResource(R.string.action_remove))
            }
        },
        dismissButton = {
            TextButton(onClick = { onAction(BudgetEditorAction.OnDismissRemoveDialog) }) {
                Text(stringResource(R.string.action_cancel))
            }
        },
        title = { Text(stringResource(R.string.remove_budget_dialog_title)) },
        text = { Text(stringResource(R.string.remove_budget_dialog_text, categoryName, monthLabel)) },
    )
}

@Preview
@Composable
private fun BudgetEditorContentLoadingPreview() {
    BudgetPilotTheme {
        Surface {
            BudgetEditorContent(state = BudgetEditorState(isLoading = true), onAction = {})
        }
    }
}

@Preview
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
