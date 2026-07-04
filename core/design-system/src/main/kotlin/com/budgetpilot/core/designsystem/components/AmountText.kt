package com.budgetpilot.core.designsystem.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.budgetpilot.core.designsystem.theme.BudgetPilotTheme
import com.budgetpilot.core.designsystem.theme.Spacing
import com.budgetpilot.core.domain.money.Money
import com.budgetpilot.core.presentation.money.PesoFormatter

/**
 * Renders a peso [amount] with tabular figures so columns of numbers align.
 */
@Composable
fun AmountText(
    amount: Money,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified,
) {
    Text(
        text = PesoFormatter.format(amount),
        modifier = modifier,
        style = style.copy(fontFeatureSettings = "tnum"),
        color = color,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@PreviewLightDark
@Composable
private fun AmountTextPreview() {
    BudgetPilotTheme {
        Surface {
            Column(modifier = Modifier.padding(Spacing.medium)) {
                AmountText(
                    amount = Money.fromPesos("12,345.67"),
                    style = MaterialTheme.typography.headlineMedium,
                )
                AmountText(
                    amount = Money.fromPesos("-50.00"),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}
