package com.budgetpilot.feature.capture.presentation.confirm.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.budgetpilot.core.designsystem.components.AmountText
import com.budgetpilot.core.designsystem.components.AppCard
import com.budgetpilot.core.designsystem.theme.BudgetPilotTheme
import com.budgetpilot.core.designsystem.theme.Spacing
import com.budgetpilot.core.domain.money.Money
import com.budgetpilot.feature.capture.domain.model.LineItem
import com.budgetpilot.feature.capture.presentation.R

private val MinTouchTargetSize = 48.dp

/** Editable, collapsible list of receipt line items (DESIGN-SPEC.md §9); display/verification state only. */
@Composable
fun LineItemsCard(
    lineItems: List<LineItem>,
    isExpanded: Boolean,
    onToggleClick: () -> Unit,
    onAddItemClick: () -> Unit,
    onEditItemClick: (index: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (lineItems.isEmpty()) {
        EmptyLineItemsBox(onClick = onAddItemClick, modifier = modifier)
        return
    }

    AppCard(
        modifier = modifier.animateContentSize(),
        contentPadding = PaddingValues(horizontal = Spacing.medium, vertical = 12.dp),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = MinTouchTargetSize)
                    .clickable(onClick = onToggleClick),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = pluralStringResource(R.plurals.line_items_count, lineItems.size, lineItems.size),
                style = MaterialTheme.typography.titleSmall,
            )
            Icon(
                imageVector = if (isExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                contentDescription = null,
            )
        }
        if (isExpanded) {
            lineItems.forEachIndexed { index, item ->
                LineItemRow(item = item, onEditClick = { onEditItemClick(index) })
            }
            AddLineItemRow(onClick = onAddItemClick)
        }
    }
}

@Composable
private fun LineItemRow(
    item: LineItem,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(top = Spacing.small),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.small),
    ) {
        Text(
            text = item.description,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
        )
        AmountText(amount = item.amount, style = MaterialTheme.typography.bodyMedium)
        IconButton(onClick = onEditClick) {
            Icon(
                imageVector = Icons.Filled.Edit,
                contentDescription = stringResource(R.string.cd_edit_line_item, item.description),
            )
        }
    }
}

@Composable
private fun AddLineItemRow(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .heightIn(min = MinTouchTargetSize)
                .clickable(onClick = onClick)
                .padding(top = Spacing.small),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.extraSmall),
    ) {
        Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp),
        )
        Text(
            text = stringResource(R.string.action_add_item),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun EmptyLineItemsBox(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .dashedBorder(color = MaterialTheme.colorScheme.outline)
                .clickable(onClick = onClick)
                .padding(vertical = Spacing.large),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.extraSmall),
    ) {
        Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = stringResource(R.string.action_add_item),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview
@Composable
private fun LineItemsCardPreview() {
    BudgetPilotTheme {
        Surface {
            LineItemsCard(
                lineItems =
                    listOf(
                        LineItem("1pc Chickenjoy w/ Rice", Money.fromPesos("89.00")),
                        LineItem("Jollibee Spaghetti", Money.fromPesos("65.00")),
                    ),
                isExpanded = true,
                onToggleClick = {},
                onAddItemClick = {},
                onEditItemClick = {},
            )
        }
    }
}

@Preview
@Composable
private fun LineItemsCardEmptyPreview() {
    BudgetPilotTheme {
        Surface {
            LineItemsCard(
                lineItems = emptyList(),
                isExpanded = false,
                onToggleClick = {},
                onAddItemClick = {},
                onEditItemClick = {},
            )
        }
    }
}
