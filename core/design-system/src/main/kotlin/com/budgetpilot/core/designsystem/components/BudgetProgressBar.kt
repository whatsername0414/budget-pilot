@file:Suppress("MatchingDeclarationName")

package com.budgetpilot.core.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.budgetpilot.core.designsystem.R
import com.budgetpilot.core.designsystem.theme.BudgetPilotTheme
import com.budgetpilot.core.designsystem.theme.Spacing
import com.budgetpilot.core.domain.money.Money

enum class BudgetStatus { ON_TRACK, WARNING, OVER_BUDGET }

private const val WARNING_THRESHOLD_PERCENT = 80.0
private const val OVER_BUDGET_THRESHOLD_PERCENT = 100.0

fun budgetStatusFor(
    spent: Money,
    budget: Money,
): BudgetStatus {
    val percent = spent.percentOf(budget)
    return when {
        percent >= OVER_BUDGET_THRESHOLD_PERCENT -> BudgetStatus.OVER_BUDGET
        percent >= WARNING_THRESHOLD_PERCENT -> BudgetStatus.WARNING
        else -> BudgetStatus.ON_TRACK
    }
}

private data class BudgetStatusPresentation(
    val color: Color,
    val icon: ImageVector,
    val label: String,
)

@Composable
private fun BudgetStatus.presentation(): BudgetStatusPresentation =
    when (this) {
        BudgetStatus.ON_TRACK ->
            BudgetStatusPresentation(
                color = MaterialTheme.colorScheme.tertiary,
                icon = Icons.Filled.CheckCircle,
                label = stringResource(R.string.budget_status_on_track),
            )
        BudgetStatus.WARNING ->
            BudgetStatusPresentation(
                color = BudgetPilotTheme.extendedColors.warning,
                icon = Icons.Filled.Warning,
                label = stringResource(R.string.budget_status_warning),
            )
        BudgetStatus.OVER_BUDGET ->
            BudgetStatusPresentation(
                color = MaterialTheme.colorScheme.error,
                icon = Icons.Filled.Warning,
                label = stringResource(R.string.budget_status_over),
            )
    }

/**
 * Budget progress with a green/amber/red threshold — always paired with an
 * icon and status text so the state is never conveyed by color alone.
 */
@Composable
fun BudgetProgressBar(
    spent: Money,
    budget: Money,
    modifier: Modifier = Modifier,
    label: String? = null,
    showRemaining: Boolean = false,
) {
    val status = budgetStatusFor(spent, budget)
    val presentation = status.presentation()
    val progress =
        if (budget == Money.ZERO) {
            0f
        } else {
            (spent.percentOf(budget) / 100.0).toFloat().coerceIn(0f, 1f)
        }

    Column(modifier = modifier.fillMaxWidth()) {
        if (label != null) {
            Text(text = label, style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(Spacing.extraSmall))
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
            color = presentation.color,
            trackColor = presentation.color.copy(alpha = 0.16f),
        )
        Spacer(Modifier.height(Spacing.extraSmall))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = presentation.icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = presentation.color,
            )
            Spacer(Modifier.width(Spacing.extraSmall))
            Text(
                text = presentation.label,
                style = MaterialTheme.typography.labelMedium,
                color = presentation.color,
                modifier = Modifier.weight(1f),
            )
            if (showRemaining) {
                AmountText(amount = budget - spent, style = MaterialTheme.typography.labelMedium)
                Text(
                    text = " ${stringResource(R.string.budget_left)}",
                    style = MaterialTheme.typography.labelMedium,
                )
            } else {
                AmountText(amount = spent, style = MaterialTheme.typography.labelMedium)
                Text(
                    text = " ${stringResource(R.string.budget_of)} ",
                    style = MaterialTheme.typography.labelMedium,
                )
                AmountText(amount = budget, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Preview
@Composable
private fun BudgetProgressBarPreview() {
    BudgetPilotTheme {
        Surface {
            Column(
                modifier = Modifier.padding(Spacing.medium),
                verticalArrangement = Arrangement.spacedBy(Spacing.medium),
            ) {
                BudgetProgressBar(
                    label = "Groceries",
                    spent = Money.fromPesos("2,400.00"),
                    budget = Money.fromPesos("6,000.00"),
                )
                BudgetProgressBar(
                    label = "Dining out",
                    spent = Money.fromPesos("4,300.00"),
                    budget = Money.fromPesos("5,000.00"),
                )
                BudgetProgressBar(
                    label = "Transport",
                    spent = Money.fromPesos("3,800.00"),
                    budget = Money.fromPesos("3,000.00"),
                )
            }
        }
    }
}
