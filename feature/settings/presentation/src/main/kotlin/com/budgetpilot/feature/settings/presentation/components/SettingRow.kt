package com.budgetpilot.feature.settings.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.tooling.preview.Preview
import com.budgetpilot.core.designsystem.theme.BudgetPilotTheme
import com.budgetpilot.core.designsystem.theme.Spacing

private const val DISABLED_ALPHA = 0.38f

/**
 * A single row within a Settings card (DESIGN-SPEC.md §12): title, a full-sentence description,
 * and trailing content — usually a [Switch] but also used for the API-key status chip. The whole
 * row is tappable when [onClick] is supplied, not just the trailing content. [enabled] dims the
 * row (Material's standard 0.38 disabled-content alpha) and drops the row-level click handler —
 * used e.g. for the Cloud AI row while private mode forces it off; the [trailingContent] switch's
 * own `enabled` still needs setting separately by the caller.
 */
@Composable
fun SettingRow(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
    trailingContent: @Composable () -> Unit = {},
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .then(if (onClick != null && enabled) Modifier.clickable(onClick = onClick) else Modifier)
                .alpha(if (enabled) 1f else DISABLED_ALPHA)
                .padding(vertical = Spacing.small),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(modifier = Modifier.width(Spacing.medium))
        trailingContent()
    }
}

@Preview
@Composable
private fun SettingRowPreview() {
    BudgetPilotTheme {
        Surface {
            SettingRow(
                title = "Use cloud AI",
                description = "Photos and questions are sent to Google Gemini to read your receipts.",
                onClick = {},
                trailingContent = { Switch(checked = true, onCheckedChange = null) },
            )
        }
    }
}

@Preview
@Composable
private fun SettingRowDisabledPreview() {
    BudgetPilotTheme {
        Surface {
            SettingRow(
                title = "Use cloud AI",
                description = "Cloud AI is off while private mode is on.",
                enabled = false,
                trailingContent = { Switch(checked = false, enabled = false, onCheckedChange = null) },
            )
        }
    }
}
