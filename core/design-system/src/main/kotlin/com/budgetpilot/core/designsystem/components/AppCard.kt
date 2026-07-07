package com.budgetpilot.core.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.budgetpilot.core.designsystem.theme.BudgetPilotTheme
import com.budgetpilot.core.designsystem.theme.Spacing

private val AppCardShape = RoundedCornerShape(16.dp)

/**
 * Generic elevated container for feature content. Slot API so callers own
 * their own internal layout.
 */
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(Spacing.medium),
    content: @Composable ColumnScope.() -> Unit,
) {
    // Mockup .card always carries a hairline border (design/mockups.html), resolving
    // DESIGN-SPEC.md §1.2's "if elevation contrast is insufficient" in favor of "always".
    val border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    if (onClick != null) {
        Card(onClick = onClick, modifier = modifier, shape = AppCardShape, border = border) {
            Column(modifier = Modifier.padding(contentPadding), content = content)
        }
    } else {
        Card(modifier = modifier, shape = AppCardShape, border = border) {
            Column(modifier = Modifier.padding(contentPadding), content = content)
        }
    }
}

@Preview
@Composable
private fun AppCardPreview() {
    BudgetPilotTheme {
        Surface(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.background) {
            AppCard(modifier = Modifier.padding(Spacing.medium)) {
                Text("Food & Dining", style = MaterialTheme.typography.titleMedium)
                Text(
                    "₱3,200.00 spent this month",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
