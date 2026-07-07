package com.budgetpilot.feature.capture.presentation.confirm.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.budgetpilot.core.designsystem.components.AmountText
import com.budgetpilot.core.designsystem.components.AppCard
import com.budgetpilot.core.designsystem.theme.BudgetPilotTheme
import com.budgetpilot.core.designsystem.theme.Spacing
import com.budgetpilot.core.domain.money.Money
import com.budgetpilot.feature.capture.domain.model.LineItem
import com.budgetpilot.feature.capture.presentation.R

/** Read-only, collapsible list of extracted receipt line items (DESIGN-SPEC.md §9). */
@Composable
fun LineItemsCard(
    lineItems: List<LineItem>,
    isExpanded: Boolean,
    onToggleClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AppCard(
        modifier = modifier.animateContentSize(),
        contentPadding = PaddingValues(horizontal = Spacing.medium, vertical = 12.dp),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
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
            lineItems.forEach { item ->
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(top = Spacing.small),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f),
                    )
                    AmountText(amount = item.amount, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
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
            )
        }
    }
}
