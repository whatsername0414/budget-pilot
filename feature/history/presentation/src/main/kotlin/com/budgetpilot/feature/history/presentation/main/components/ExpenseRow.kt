package com.budgetpilot.feature.history.presentation.main.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.budgetpilot.core.designsystem.components.AmountText
import com.budgetpilot.core.designsystem.icons.categoryIcon
import com.budgetpilot.core.designsystem.theme.BudgetPilotTheme
import com.budgetpilot.core.designsystem.theme.Spacing
import com.budgetpilot.core.designsystem.theme.categoryColor
import com.budgetpilot.core.domain.model.ExpenseSource
import com.budgetpilot.core.domain.money.Money
import com.budgetpilot.feature.history.presentation.R
import com.budgetpilot.feature.history.presentation.main.model.ExpenseUi

private val ExpenseRowVerticalPadding = 10.dp
private val ExpenseRowItemGap = 12.dp
private val ExpenseRowMetaGap = 6.dp

/** DESIGN-SPEC.md §1.4 anatomy: 40dp tinted icon · merchant/category · amount/time. Row is the full touch target. */
@Composable
fun ExpenseRow(
    expense: ExpenseUi,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    val color = categoryColor(expense.categoryColorKey)

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .heightIn(min = 60.dp)
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onClick)
                .padding(horizontal = Spacing.small, vertical = ExpenseRowVerticalPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(ExpenseRowItemGap),
    ) {
        Box(
            modifier =
                Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = categoryIcon(expense.categoryIconKey),
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp),
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = expense.merchant,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(ExpenseRowMetaGap),
            ) {
                Text(
                    text =
                        if (expense.source != ExpenseSource.MANUAL) {
                            stringResource(R.string.expense_row_category_with_source, expense.categoryName)
                        } else {
                            expense.categoryName
                        },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (expense.source != ExpenseSource.MANUAL) {
                    SourceBadge(source = expense.source)
                }
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            AmountText(
                amount = expense.amount,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            )
            Text(
                text = expense.formattedTime,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SourceBadge(
    source: ExpenseSource,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(4.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
    ) {
        Text(
            text = source.name,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(horizontal = Spacing.extraSmall),
        )
    }
}

@Preview
@Composable
private fun ExpenseRowPreview() {
    BudgetPilotTheme {
        Surface {
            Column {
                ExpenseRow(
                    expense =
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
                )
                ExpenseRow(
                    expense =
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
                )
            }
        }
    }
}
