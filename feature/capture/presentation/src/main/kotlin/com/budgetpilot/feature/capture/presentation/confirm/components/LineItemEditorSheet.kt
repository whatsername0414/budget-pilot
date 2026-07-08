package com.budgetpilot.feature.capture.presentation.confirm.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.budgetpilot.core.designsystem.theme.BudgetPilotTheme
import com.budgetpilot.core.designsystem.theme.Spacing
import com.budgetpilot.feature.capture.presentation.R
import com.budgetpilot.feature.capture.presentation.confirm.ConfirmExpenseAction
import com.budgetpilot.feature.capture.presentation.confirm.ConfirmExpenseState

/** Add/edit sheet for a single receipt line item (DESIGN-SPEC.md §9); prices only ever affect [ConfirmExpenseState.lineItems]. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LineItemEditorSheet(
    state: ConfirmExpenseState,
    onAction: (ConfirmExpenseAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    ModalBottomSheet(
        onDismissRequest = { onAction(ConfirmExpenseAction.OnDismissLineItemSheet) },
        modifier = modifier,
        sheetState = rememberModalBottomSheetState(),
    ) {
        LineItemEditorContent(state = state, onAction = onAction)
    }
}

@Composable
private fun LineItemEditorContent(
    state: ConfirmExpenseState,
    onAction: (ConfirmExpenseAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isEditing = state.editingLineItemIndex != null

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.medium, vertical = Spacing.small),
        verticalArrangement = Arrangement.spacedBy(Spacing.medium),
    ) {
        Text(
            text =
                stringResource(
                    if (isEditing) R.string.line_item_editor_title_edit else R.string.line_item_editor_title_add,
                ),
            style = MaterialTheme.typography.titleMedium,
        )
        OutlinedTextField(
            value = state.lineItemDraftDescription,
            onValueChange = { onAction(ConfirmExpenseAction.OnLineItemDescriptionChange(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.label_item_name)) },
            singleLine = true,
        )
        OutlinedTextField(
            value = state.lineItemDraftPriceText,
            onValueChange = { onAction(ConfirmExpenseAction.OnLineItemPriceChange(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.label_item_price)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.small),
        ) {
            if (isEditing) {
                TextButton(onClick = { onAction(ConfirmExpenseAction.OnRemoveLineItemClick) }) {
                    Text(text = stringResource(R.string.action_remove), color = MaterialTheme.colorScheme.error)
                }
            }
            Spacer(Modifier.weight(1f))
            TextButton(onClick = { onAction(ConfirmExpenseAction.OnDismissLineItemSheet) }) {
                Text(stringResource(R.string.action_cancel))
            }
            Button(
                onClick = { onAction(ConfirmExpenseAction.OnSaveLineItemClick) },
                enabled = state.isLineItemDraftValid,
            ) {
                Text(stringResource(R.string.action_save))
            }
        }
    }
}

@Preview
@Composable
private fun LineItemEditorContentAddPreview() {
    BudgetPilotTheme {
        Surface {
            LineItemEditorContent(state = ConfirmExpenseState(), onAction = {})
        }
    }
}

@Preview
@Composable
private fun LineItemEditorContentEditPreview() {
    BudgetPilotTheme {
        Surface {
            LineItemEditorContent(
                state =
                    ConfirmExpenseState(
                        editingLineItemIndex = 0,
                        lineItemDraftDescription = "1pc Chickenjoy w/ Rice",
                        lineItemDraftPriceText = "89.00",
                    ),
                onAction = {},
            )
        }
    }
}
