package com.budgetpilot.feature.ask.presentation.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.budgetpilot.core.designsystem.icons.StateIcons
import com.budgetpilot.core.designsystem.theme.BudgetPilotTheme
import com.budgetpilot.core.designsystem.theme.Spacing
import com.budgetpilot.feature.ask.presentation.R

/** No conversation yet: sparkle icon, prompt, and the four fixed suggestion chips (DESIGN-SPEC.md §10). */
@Composable
fun AskEmptyState(
    onSuggestionClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(Spacing.large),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.medium),
    ) {
        Icon(
            imageVector = StateIcons.Sparkle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = stringResource(R.string.ask_empty_title),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
        )
        val suggestions =
            listOf(
                stringResource(R.string.suggestion_food_this_month),
                stringResource(R.string.suggestion_over_budget),
                stringResource(R.string.suggestion_biggest_expense_week),
                stringResource(R.string.suggestion_compare_months),
            )
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(Spacing.small),
        ) {
            suggestions.forEach { suggestion ->
                AssistChip(onClick = { onSuggestionClick(suggestion) }, label = { Text(suggestion) })
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun AskEmptyStatePreview() {
    BudgetPilotTheme {
        Surface {
            AskEmptyState(onSuggestionClick = {})
        }
    }
}
