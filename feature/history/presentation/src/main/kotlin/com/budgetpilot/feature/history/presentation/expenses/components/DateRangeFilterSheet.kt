package com.budgetpilot.feature.history.presentation.expenses.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.budgetpilot.core.designsystem.theme.BudgetPilotTheme
import com.budgetpilot.core.designsystem.theme.Spacing
import com.budgetpilot.feature.history.presentation.R
import com.budgetpilot.feature.history.presentation.expenses.model.DateRangePreset

/** Bottom sheet content for the "Filters" chip — date-range presets only (DESIGN-SPEC.md §4). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangeFilterSheet(
    selectedPreset: DateRangePreset,
    onPresetSelect: (DateRangePreset) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(),
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier,
        sheetState = sheetState,
    ) {
        Column(modifier = Modifier.padding(bottom = Spacing.large)) {
            Text(
                text = stringResource(R.string.date_range_sheet_title),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = Spacing.medium, vertical = Spacing.small),
            )
            DateRangePreset.entries.forEach { preset ->
                val selected = preset == selectedPreset
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .selectable(selected = selected, onClick = { onPresetSelect(preset) })
                            .padding(horizontal = Spacing.medium, vertical = Spacing.extraSmall),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Row already carries the click via .selectable(); a second click target on
                    // the RadioButton itself would make TalkBack stop on this row twice.
                    RadioButton(selected = selected, onClick = null)
                    Text(
                        text = preset.label,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = Spacing.small),
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun DateRangeFilterSheetPreview() {
    BudgetPilotTheme {
        Column(modifier = Modifier.padding(Spacing.medium)) {
            DateRangePreset.entries.forEach { preset ->
                Text(preset.label, modifier = Modifier.padding(vertical = 2.dp))
            }
        }
    }
}
