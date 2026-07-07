package com.budgetpilot.feature.settings.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.unit.dp
import com.budgetpilot.core.designsystem.theme.BudgetPilotTheme

private const val DISABLED_ALPHA = 0.38f
private val SettingRowContentGap = 14.dp
private val SettingRowVerticalPadding = 14.dp
private val SettingRowTextGap = 2.dp

/**
 * A single row within a Settings card (DESIGN-SPEC.md §12): title, a full-sentence description,
 * and trailing content — usually a [Switch] but also used for the API-key status chip. The whole
 * row is tappable when [onClick] is supplied, not just the trailing content. [enabled] dims the
 * row (Material's standard 0.38 disabled-content alpha) and drops the row-level click handler —
 * used e.g. for the Cloud AI row while private mode forces it off; the [trailingContent] switch's
 * own `enabled` still needs setting separately by the caller. [verticalAlignment] defaults to
 * [Alignment.Top] (mockup rows with a two-line description) — single-line rows like the API-key
 * status chip and About's Version pass [Alignment.CenterVertically] instead.
 */
@Composable
fun SettingRow(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    verticalAlignment: Alignment.Vertical = Alignment.Top,
    onClick: (() -> Unit)? = null,
    trailingContent: @Composable () -> Unit = {},
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .then(if (onClick != null && enabled) Modifier.clickable(onClick = onClick) else Modifier)
                .alpha(if (enabled) 1f else DISABLED_ALPHA)
                .padding(vertical = SettingRowVerticalPadding),
        verticalAlignment = verticalAlignment,
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(SettingRowTextGap)) {
            Text(text = title, style = MaterialTheme.typography.titleSmall)
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(modifier = Modifier.width(SettingRowContentGap))
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
