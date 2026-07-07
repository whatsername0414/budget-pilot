package com.budgetpilot.core.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.budgetpilot.core.designsystem.R
import com.budgetpilot.core.designsystem.theme.BudgetPilotTheme
import com.budgetpilot.core.designsystem.theme.Spacing

// design/mockups.html .minibtn/.minibtn.o: 40dp-tall pill buttons, 20dp horizontal padding,
// SemiBold label; the secondary escape hatch is outlined, not a borderless text button.
private val ActionButtonPadding = PaddingValues(horizontal = 20.dp, vertical = Spacing.small)

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
            contentPadding = ActionButtonPadding,
        ) {
            Text(retryLabel, fontWeight = FontWeight.SemiBold)
        }
        if (secondaryActionLabel != null && onSecondaryAction != null) {
            OutlinedButton(onClick = onSecondaryAction, contentPadding = ActionButtonPadding) {
                Text(secondaryActionLabel, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Preview
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
