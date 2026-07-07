package com.budgetpilot.core.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import com.budgetpilot.core.designsystem.theme.BudgetPilotTheme
import com.budgetpilot.core.designsystem.theme.Spacing

/** Selectable chip for a spending category, colored with that category's token. */
@Composable
fun CategoryChip(
    label: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    onClick: () -> Unit = {},
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
            )
        },
        colors =
            FilterChipDefaults.filterChipColors(
                selectedContainerColor = color.copy(alpha = 0.18f),
                selectedLabelColor = color,
                selectedLeadingIconColor = color,
            ),
        modifier = modifier,
    )
}

@Preview
@Composable
private fun CategoryChipPreview() {
    BudgetPilotTheme {
        Surface {
            Row(
                modifier = Modifier.padding(Spacing.medium),
                horizontalArrangement = Arrangement.spacedBy(Spacing.small),
            ) {
                CategoryChip(
                    label = "Food",
                    icon = Icons.Filled.ShoppingCart,
                    color = Color(0xFFDC2626),
                    selected = true,
                )
                CategoryChip(
                    label = "Transport",
                    icon = Icons.Filled.ShoppingCart,
                    color = Color(0xFF3B82F6),
                )
            }
        }
    }
}
