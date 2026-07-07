package com.budgetpilot.feature.ask.presentation.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.budgetpilot.core.designsystem.theme.BudgetPilotTheme
import com.budgetpilot.core.designsystem.theme.Spacing

/** 1-2 follow-up suggestions shown after an answer (DESIGN-SPEC.md §10); tapping submits it as the next question. */
@Composable
fun AskFollowUpChips(
    suggestions: List<String>,
    onSuggestionClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(Spacing.small),
    ) {
        suggestions.forEach { suggestion ->
            AssistChip(onClick = { onSuggestionClick(suggestion) }, label = { Text(suggestion) })
        }
    }
}

@Preview
@Composable
private fun AskFollowUpChipsPreview() {
    BudgetPilotTheme {
        Surface {
            AskFollowUpChips(
                suggestions = listOf("Compare to last month?", "Break down by merchant?"),
                onSuggestionClick = {},
            )
        }
    }
}
