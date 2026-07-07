package com.budgetpilot.core.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.budgetpilot.core.designsystem.theme.BudgetPilotTheme
import com.budgetpilot.core.designsystem.theme.Spacing

/** Title for a screen section, with an optional trailing action (e.g. "See all"). */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    action: @Composable (() -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = title, style = MaterialTheme.typography.titleMedium)
        action?.invoke()
    }
}

@Preview
@Composable
private fun SectionHeaderPreview() {
    BudgetPilotTheme {
        Surface {
            SectionHeader(
                title = "Recent expenses",
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(Spacing.medium),
                action = { TextButton(onClick = {}) { Text("See all") } },
            )
        }
    }
}
