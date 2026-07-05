package com.budgetpilot.feature.budgets.presentation.main.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.budgetpilot.core.designsystem.theme.BudgetPilotTheme

/** DESIGN-SPEC.md §6: chevrons + month label; forward chevron disabled beyond the current month. */
@Composable
fun MonthSelector(
    monthLabel: String,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    isNextEnabled: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onPreviousClick) {
            Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous month")
        }
        Text(text = monthLabel, style = MaterialTheme.typography.titleMedium)
        IconButton(onClick = onNextClick, enabled = isNextEnabled) {
            Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next month")
        }
    }
}

@PreviewLightDark
@Composable
private fun MonthSelectorPreview() {
    BudgetPilotTheme {
        Surface {
            MonthSelector(
                monthLabel = "July 2026",
                onPreviousClick = {},
                onNextClick = {},
                isNextEnabled = false,
            )
        }
    }
}
