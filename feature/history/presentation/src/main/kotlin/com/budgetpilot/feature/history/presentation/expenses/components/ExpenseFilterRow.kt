package com.budgetpilot.feature.history.presentation.expenses.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.budgetpilot.core.designsystem.components.CategoryChip
import com.budgetpilot.core.designsystem.icons.categoryIcon
import com.budgetpilot.core.designsystem.theme.BudgetPilotTheme
import com.budgetpilot.core.designsystem.theme.Spacing
import com.budgetpilot.core.designsystem.theme.categoryColor
import com.budgetpilot.core.domain.model.Category
import com.budgetpilot.feature.history.presentation.R

/** DESIGN-SPEC.md §4: search pill → "Filters" chip (opens date-range sheet) → per-category chips. */
@Composable
fun ExpenseSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text(stringResource(R.string.search_placeholder_merchant_note)) },
        leadingIcon = { Icon(imageVector = Icons.Filled.Search, contentDescription = null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(imageVector = Icons.Filled.Close, contentDescription = stringResource(R.string.cd_clear_search))
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(28.dp),
        colors = OutlinedTextFieldDefaults.colors(),
    )
}

@Composable
fun ExpenseFilterChipRow(
    categories: List<Category>,
    selectedCategoryId: Long?,
    hasDateFilterActive: Boolean,
    onFiltersClick: () -> Unit,
    onCategorySelect: (Long?) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.small),
        contentPadding = PaddingValues(horizontal = Spacing.medium),
    ) {
        item {
            AssistChip(
                onClick = onFiltersClick,
                label = { Text(stringResource(R.string.action_filters)) },
                leadingIcon = {
                    Icon(imageVector = FilterIcons.Tune, contentDescription = null)
                },
                colors =
                    if (hasDateFilterActive) {
                        AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            labelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            leadingIconContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                    } else {
                        AssistChipDefaults.assistChipColors()
                    },
            )
        }
        items(items = categories, key = { it.id }) { category ->
            CategoryChip(
                label = category.name,
                icon = categoryIcon(category.iconKey),
                color = categoryColor(category.colorKey),
                selected = category.id == selectedCategoryId,
                onClick = {
                    onCategorySelect(if (category.id == selectedCategoryId) null else category.id)
                },
            )
        }
    }
}

@Preview
@Composable
private fun ExpenseFilterRowPreview() {
    BudgetPilotTheme {
        Surface {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
                ExpenseSearchField(
                    query = "",
                    onQueryChange = {},
                    modifier = Modifier.padding(horizontal = Spacing.medium),
                )
                ExpenseFilterChipRow(
                    categories =
                        listOf(
                            Category(1, "Food", "restaurant", "food", true),
                            Category(2, "Transport", "directions_bus", "transport", true),
                        ),
                    selectedCategoryId = 1,
                    hasDateFilterActive = true,
                    onFiltersClick = {},
                    onCategorySelect = {},
                )
            }
        }
    }
}
