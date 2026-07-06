package com.budgetpilot.core.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.budgetpilot.core.designsystem.R
import com.budgetpilot.core.designsystem.theme.BudgetPilotTheme
import com.budgetpilot.core.designsystem.theme.Spacing

/** Error message + retry action (and an optional secondary escape hatch), for a failed load/operation. */
@Composable
fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    retryLabel: String = stringResource(R.string.action_retry),
    secondaryActionLabel: String? = null,
    onSecondaryAction: (() -> Unit)? = null,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(Spacing.large),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.extraSmall),
    ) {
        Icon(
            imageVector = Icons.Filled.Warning,
            contentDescription = stringResource(R.string.cd_error_icon),
            modifier = Modifier.padding(bottom = Spacing.small),
            tint = MaterialTheme.colorScheme.error,
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )
        Button(
            onClick = onRetry,
            modifier = Modifier.padding(top = Spacing.small),
        ) {
            Text(retryLabel)
        }
        if (secondaryActionLabel != null && onSecondaryAction != null) {
            TextButton(onClick = onSecondaryAction) {
                Text(secondaryActionLabel)
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun ErrorStatePreview() {
    BudgetPilotTheme {
        Surface {
            ErrorState(
                message = "We couldn't load your expenses. Check your connection and try again.",
                onRetry = {},
            )
        }
    }
}
