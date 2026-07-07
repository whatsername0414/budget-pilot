package com.budgetpilot.feature.budgets.presentation.charts.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.budgetpilot.core.designsystem.components.AmountText
import com.budgetpilot.core.designsystem.theme.BudgetPilotTheme
import com.budgetpilot.core.designsystem.theme.Spacing
import com.budgetpilot.core.designsystem.theme.categoryColor
import com.budgetpilot.core.domain.money.Money
import com.budgetpilot.core.presentation.money.PesoFormatter
import com.budgetpilot.feature.budgets.presentation.charts.model.CategorySpendUi

private val BarShape = RoundedCornerShape(4.dp)
private val CategoryLabelWidth = 96.dp
private val CategoryValueWidth = 74.dp
private val CategorySpendRowGap = 10.dp

/**
 * Horizontal bars, sorted descending, with direct value labels — never a pie
 * chart (per PLAN.md §4.4: weak accessibility beyond 5 categories).
 */
@Composable
fun CategorySpendChart(
    categorySpend: List<CategorySpendUi>,
    modifier: Modifier = Modifier,
) {
    val top = categorySpend.firstOrNull()
    val summary =
        if (top != null) {
            "Spending by category. ${top.name} is highest at ${PesoFormatter.format(top.amount)}."
        } else {
            "Spending by category."
        }
    Column(
        modifier = modifier.semantics { contentDescription = summary },
        verticalArrangement = Arrangement.spacedBy(Spacing.small),
    ) {
        categorySpend.forEach { category ->
            CategorySpendRow(category)
        }
    }
}

@Composable
private fun CategorySpendRow(
    category: CategorySpendUi,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CategorySpendRowGap),
    ) {
        Text(
            text = category.name,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.width(CategoryLabelWidth),
        )
        Box(
            modifier =
                Modifier
                    .weight(1f)
                    .height(20.dp)
                    .clip(BarShape)
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.08f)),
        ) {
            Surface(
                color = categoryColor(category.colorKey),
                shape = BarShape,
                modifier =
                    Modifier
                        .fillMaxWidth(category.fraction.coerceIn(0f, 1f))
                        .fillMaxHeight(),
            ) {}
        }
        AmountText(
            amount = category.amount,
            style = MaterialTheme.typography.labelMedium.copy(textAlign = TextAlign.End),
            modifier = Modifier.width(CategoryValueWidth),
        )
    }
}

@Preview
@Composable
private fun CategorySpendChartPreview() {
    BudgetPilotTheme {
        Surface {
            CategorySpendChart(
                categorySpend =
                    listOf(
                        CategorySpendUi(1, "Food", "food", Money.fromPesos("4200.00"), 1f),
                        CategorySpendUi(2, "Transport", "transport", Money.fromPesos("2100.00"), 0.5f),
                        CategorySpendUi(3, "Bills", "bills", Money.fromPesos("950.00"), 0.226f),
                        CategorySpendUi(4, "Shopping", "shopping", Money.fromPesos("480.00"), 0.114f),
                    ),
                modifier = Modifier.width(320.dp),
            )
        }
    }
}
