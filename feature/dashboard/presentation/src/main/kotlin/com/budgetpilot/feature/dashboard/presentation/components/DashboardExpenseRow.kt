package com.budgetpilot.feature.dashboard.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.budgetpilot.core.designsystem.components.AmountText
import com.budgetpilot.core.designsystem.icons.categoryIcon
import com.budgetpilot.core.designsystem.theme.BudgetPilotTheme
import com.budgetpilot.core.designsystem.theme.Spacing
import com.budgetpilot.core.designsystem.theme.categoryColor
import com.budgetpilot.core.domain.model.ExpenseSource
import com.budgetpilot.core.domain.money.Money
import com.budgetpilot.feature.dashboard.presentation.model.DashboardExpenseUi

/** DESIGN-SPEC.md §1.4 anatomy, duplicated from :feature:expenses (features never depend on each other). */
@Composable
fun DashboardExpenseRow(
    expense: DashboardExpenseUi,
    modifier: Modifier = Modifier,
) {
    val color = categoryColor(expense.categoryColorKey)

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .heightIn(min = 60.dp)
                .padding(vertical = Spacing.small),
        verticalAlignment = Alignment.CenterVertically,
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
            )
        }

        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .padding(horizontal = Spacing.small),
        ) {
            Text(
                text = expense.merchant,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = expense.categoryName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (expense.source != ExpenseSource.MANUAL) {
                    DashboardSourceBadge(source = expense.source, modifier = Modifier.padding(start = Spacing.extraSmall))
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
private fun DashboardSourceBadge(
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

@PreviewLightDark
@Composable
private fun DashboardExpenseRowPreview() {
    BudgetPilotTheme {
        Surface {
            Column {
                DashboardExpenseRow(
                    expense =
                        DashboardExpenseUi(
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
                DashboardExpenseRow(
                    expense =
                        DashboardExpenseUi(
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
