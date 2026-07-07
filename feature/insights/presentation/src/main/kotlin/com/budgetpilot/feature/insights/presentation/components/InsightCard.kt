package com.budgetpilot.feature.insights.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.budgetpilot.core.designsystem.icons.StateIcons
import com.budgetpilot.core.designsystem.theme.BudgetPilotTheme
import com.budgetpilot.core.designsystem.theme.Spacing
import com.budgetpilot.feature.insights.presentation.R

private val InsightCardShape = RoundedCornerShape(16.dp)
private val InsightCardVerticalPadding = 14.dp
private val InsightSparkleSize = 20.dp
private val InsightDismissIconSize = 16.dp
private const val INSIGHT_DISMISS_ICON_ALPHA = 0.7f

/**
 * The dashboard's proactive-insight card (DESIGN-SPEC.md §11) — the only card in the app allowed
 * to use `primaryContainer`, and at most one exists at a time.
 */
@Composable
fun InsightCard(
    message: String,
    onDismiss: () -> Unit,
    onAskMore: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = InsightCardShape,
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ),
    ) {
        Column(modifier = Modifier.padding(horizontal = Spacing.medium, vertical = InsightCardVerticalPadding)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.small),
                verticalAlignment = Alignment.Top,
            ) {
                Icon(
                    imageVector = StateIcons.Sparkle,
                    contentDescription = null,
                    modifier = Modifier.size(InsightSparkleSize),
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.insight_card_title),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    )
                    Text(text = message, style = MaterialTheme.typography.bodyMedium)
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = stringResource(R.string.cd_dismiss_insight),
                        modifier = Modifier.size(InsightDismissIconSize).alpha(INSIGHT_DISMISS_ICON_ALPHA),
                    )
                }
            }
            Spacer(Modifier.height(Spacing.small))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onAskMore) {
                    Text(stringResource(R.string.action_ask_more))
                }
            }
        }
    }
}

@Preview
@Composable
private fun InsightCardPreview() {
    BudgetPilotTheme {
        InsightCard(
            message = "You've used 82% of your Food budget this month (₱8,200.00 of ₱10,000.00).",
            onDismiss = {},
            onAskMore = {},
        )
    }
}
