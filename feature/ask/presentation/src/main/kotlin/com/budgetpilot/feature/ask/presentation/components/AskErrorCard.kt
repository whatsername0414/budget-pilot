package com.budgetpilot.feature.ask.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.budgetpilot.core.designsystem.theme.BudgetPilotTheme
import com.budgetpilot.core.designsystem.theme.Spacing
import com.budgetpilot.feature.ask.presentation.AskErrorUi
import com.budgetpilot.feature.ask.presentation.R

private val ErrorCardShape = RoundedCornerShape(16.dp)

/** Ask errors always render in-conversation, never as toasts (DESIGN-SPEC.md §10). */
@Composable
fun AskErrorCard(
    error: AskErrorUi,
    onRetryClick: () -> Unit,
    onOpenSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isWarning = error is AskErrorUi.RateLimited
    val containerColor =
        if (isWarning) {
            BudgetPilotTheme.extendedColors.warningContainer
        } else {
            MaterialTheme.colorScheme.errorContainer
        }
    val contentColor =
        if (isWarning) {
            BudgetPilotTheme.extendedColors.onWarningContainer
        } else {
            MaterialTheme.colorScheme.onErrorContainer
        }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = ErrorCardShape,
        colors = CardDefaults.cardColors(containerColor = containerColor, contentColor = contentColor),
    ) {
        Column(modifier = Modifier.padding(Spacing.medium)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.small),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Filled.Warning,
                    contentDescription =
                        stringResource(if (isWarning) R.string.cd_warning_icon else R.string.cd_error_icon),
                    tint = contentColor,
                )
                Text(text = error.message(), style = MaterialTheme.typography.bodyMedium)
            }
            error.action(onRetryClick, onOpenSettingsClick)?.let { (label, onClick) ->
                TextButton(onClick = onClick, modifier = Modifier.padding(top = Spacing.extraSmall)) {
                    Text(text = label, color = contentColor)
                }
            }
        }
    }
}

@Composable
private fun AskErrorUi.message(): String =
    when (this) {
        is AskErrorUi.RateLimited ->
            stringResource(R.string.error_ask_rate_limited) + " " + stringResource(R.string.retry_countdown, retryInSeconds)
        AskErrorUi.Offline -> stringResource(R.string.error_ask_offline)
        AskErrorUi.NoApiKey -> stringResource(R.string.error_ask_no_api_key)
        AskErrorUi.Generic -> stringResource(R.string.error_ask_generic)
    }

@Composable
private fun AskErrorUi.action(
    onRetryClick: () -> Unit,
    onOpenSettingsClick: () -> Unit,
): Pair<String, () -> Unit>? =
    when (this) {
        is AskErrorUi.RateLimited -> null
        AskErrorUi.Offline, AskErrorUi.NoApiKey -> stringResource(R.string.action_open_settings) to onOpenSettingsClick
        AskErrorUi.Generic -> stringResource(R.string.action_retry) to onRetryClick
    }

@Preview
@Composable
private fun AskErrorCardRateLimitedPreview() {
    BudgetPilotTheme {
        Surface {
            AskErrorCard(
                error = AskErrorUi.RateLimited(retryInSeconds = 42),
                onRetryClick = {},
                onOpenSettingsClick = {},
            )
        }
    }
}

@Preview
@Composable
private fun AskErrorCardOfflinePreview() {
    BudgetPilotTheme {
        Surface {
            AskErrorCard(error = AskErrorUi.Offline, onRetryClick = {}, onOpenSettingsClick = {})
        }
    }
}

@Preview
@Composable
private fun AskErrorCardNoApiKeyPreview() {
    BudgetPilotTheme {
        Surface {
            AskErrorCard(error = AskErrorUi.NoApiKey, onRetryClick = {}, onOpenSettingsClick = {})
        }
    }
}

@Preview
@Composable
private fun AskErrorCardGenericPreview() {
    BudgetPilotTheme {
        Surface {
            AskErrorCard(error = AskErrorUi.Generic, onRetryClick = {}, onOpenSettingsClick = {})
        }
    }
}
