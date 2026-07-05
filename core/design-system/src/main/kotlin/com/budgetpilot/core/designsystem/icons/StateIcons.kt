package com.budgetpilot.core.designsystem.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

/**
 * `EmptyState`/`ErrorState` glyphs from the approved mockup
 * (artifact 4d800ab6, "Shared states" section) that aren't in
 * `material-icons-core`; copied from the MIT-licensed
 * `@material-design-icons/svg` "filled" set (same approach as
 * `CategoryIcons`) rather than adding `material-icons-extended`.
 */
object StateIcons {
    /** History/Dashboard/Charts "no expenses yet" empty state. */
    val Receipt: ImageVector by lazy {
        vectorOf(
            name = "Receipt",
            path =
                "M18 17H6v-2h12v2zm0-4H6v-2h12v2zm0-4H6V7h12v2zM3 22l1.5-1.5L6 22l1.5-1.5L9 " +
                    "22l1.5-1.5L12 22l1.5-1.5L15 22l1.5-1.5L18 22l1.5-1.5L21 22V2l-1.5 1.5L18 2l-1.5 " +
                    "1.5L15 2l-1.5 1.5L12 2l-1.5 1.5L9 2 7.5 3.5 6 2 4.5 3.5 3 2v20z",
        )
    }

    /** Charts "not enough data yet" (trend) empty state. */
    val Calendar: ImageVector by lazy {
        vectorOf(
            name = "Calendar",
            path =
                "M20 3h-1V1h-2v2H7V1H5v2H4c-1.1 0-2 .9-2 2v16c0 1.1.9 2 2 2h16c1.1 0 2-.9 " +
                    "2-2V5c0-1.1-.9-2-2-2zm0 18H4V8h16v13z",
        )
    }
}

private fun vectorOf(
    name: String,
    path: String,
): ImageVector =
    ImageVector
        .Builder(
            name = name,
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).addPath(
            pathData = PathParser().parsePathString(path).toNodes(),
            fill = SolidColor(Color.Black),
        ).build()
