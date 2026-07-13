package com.budgetpilot.feature.settings.presentation.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.budgetpilot.core.designsystem.components.AppCard
import com.budgetpilot.core.designsystem.components.LoadingSkeleton
import com.budgetpilot.core.designsystem.theme.BudgetPilotTheme
import com.budgetpilot.core.designsystem.theme.Shapes
import com.budgetpilot.core.designsystem.theme.Spacing
import com.budgetpilot.feature.settings.presentation.R
import com.budgetpilot.feature.settings.presentation.SettingsAction

private val AppearanceCardContentPadding = PaddingValues(horizontal = Spacing.medium, vertical = Spacing.extraSmall)
private val AppearanceSkeletonRowHeight = 40.dp

/** Settings §Appearance card (Phase 7 optional polish): Material You dynamic color, default OFF. */
@Composable
fun AppearanceCard(
    dynamicColorEnabled: Boolean,
    isLoading: Boolean,
    onAction: (SettingsAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    AppCard(modifier = modifier.fillMaxWidth(), contentPadding = AppearanceCardContentPadding) {
        if (isLoading) {
            LoadingSkeleton(
                shape = Shapes.small,
                modifier = Modifier.fillMaxWidth().height(AppearanceSkeletonRowHeight),
            )
            return@AppCard
        }
        SettingRow(
            title = stringResource(R.string.settings_dynamic_color_title),
            description = stringResource(R.string.settings_dynamic_color_description),
            onClick = { onAction(SettingsAction.OnDynamicColorToggle(!dynamicColorEnabled)) },
            trailingContent = {
                Switch(checked = dynamicColorEnabled, onCheckedChange = null)
            },
        )
    }
}

@Preview
@Composable
private fun AppearanceCardPreview() {
    BudgetPilotTheme {
        AppearanceCard(dynamicColorEnabled = false, isLoading = false, onAction = {})
    }
}
