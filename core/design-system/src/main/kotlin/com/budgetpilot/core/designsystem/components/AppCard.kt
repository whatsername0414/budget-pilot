package com.budgetpilot.core.designsystem.components

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
import androidx.compose.ui.tooling.preview.PreviewLightDark
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
    if (onClick != null) {
        Card(onClick = onClick, modifier = modifier, shape = AppCardShape) {
            Column(modifier = Modifier.padding(contentPadding), content = content)
        }
    } else {
        Card(modifier = modifier, shape = AppCardShape) {
            Column(modifier = Modifier.padding(contentPadding), content = content)
        }
    }
}

@PreviewLightDark
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
