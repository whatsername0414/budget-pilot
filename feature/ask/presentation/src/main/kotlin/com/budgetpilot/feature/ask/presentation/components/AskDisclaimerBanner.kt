package com.budgetpilot.feature.ask.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.budgetpilot.core.designsystem.theme.BudgetPilotTheme
import com.budgetpilot.core.designsystem.theme.HairlineBorderWidth
import com.budgetpilot.core.designsystem.theme.Shapes
import com.budgetpilot.core.designsystem.theme.Spacing
import com.budgetpilot.feature.ask.presentation.R

/** Persistent free-tier/no-memory disclaimer shown above the conversation. */
@Composable
fun AskDisclaimerBanner(modifier: Modifier = Modifier) {
    val warning = BudgetPilotTheme.extendedColors.warning

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = Shapes.medium,
        colors = CardDefaults.cardColors(containerColor = warning.copy(alpha = 0.16f)),
        border = BorderStroke(HairlineBorderWidth, warning),
    ) {
        Text(
            modifier = Modifier.padding(Spacing.medium),
            text = stringResource(R.string.ask_disclaimer),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview
@Composable
private fun AskDisclaimerBannerPreview() {
    BudgetPilotTheme {
        Surface {
            AskDisclaimerBanner()
        }
    }
}
