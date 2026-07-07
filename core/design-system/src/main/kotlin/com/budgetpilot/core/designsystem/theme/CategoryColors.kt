package com.budgetpilot.core.designsystem.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** Category identity colors from DESIGN-SPEC.md §1.3 — identity, not status. */
private val CategoryColorMap =
    mapOf(
        "food" to CategoryFood,
        "transport" to CategoryTransport,
        "bills" to CategoryBills,
        "groceries" to CategoryGroceries,
        "shopping" to CategoryShopping,
        "health" to CategoryHealth,
        "entertainment" to CategoryEntertainment,
        "other" to CategoryOther,
    )

/** Maps a seeded category's `colorKey` to its color; unrecognized keys fall back to "other". */
@Composable
fun categoryColor(colorKey: String): Color = CategoryColorMap[colorKey] ?: CategoryColorMap.getValue("other")
