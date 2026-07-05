package com.budgetpilot.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** Category identity colors from DESIGN-SPEC.md §1.3 — identity, not status. */
private val CategoryColorsLight =
    mapOf(
        "food" to CategoryFoodLight,
        "transport" to CategoryTransportLight,
        "bills" to CategoryBillsLight,
        "groceries" to CategoryGroceriesLight,
        "shopping" to CategoryShoppingLight,
        "health" to CategoryHealthLight,
        "entertainment" to CategoryEntertainmentLight,
        "other" to CategoryOtherLight,
    )

private val CategoryColorsDark =
    mapOf(
        "food" to CategoryFoodDark,
        "transport" to CategoryTransportDark,
        "bills" to CategoryBillsDark,
        "groceries" to CategoryGroceriesDark,
        "shopping" to CategoryShoppingDark,
        "health" to CategoryHealthDark,
        "entertainment" to CategoryEntertainmentDark,
        "other" to CategoryOtherDark,
    )

/** Maps a seeded category's `colorKey` to its theme-appropriate color; unrecognized keys fall back to "other". */
@Composable
fun categoryColor(colorKey: String): Color {
    val palette = if (isSystemInDarkTheme()) CategoryColorsDark else CategoryColorsLight
    return palette[colorKey] ?: palette.getValue("other")
}
