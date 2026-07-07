package com.budgetpilot.feature.history.presentation.main.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

/**
 * "Tune" isn't in `material-icons-core`; copied from the MIT-licensed
 * `@material-design-icons/svg` "filled" set (same approach as
 * `:core:design-system`'s `CategoryIcons`) rather than adding
 * `material-icons-extended`.
 */
internal val FilterIcons.Tune: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "Tune",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).addPath(
            pathData =
                PathParser()
                    .parsePathString(
                        "M3 17v2h6v-2H3zM3 5v2h10V5H3zm10 16v-2h8v-2h-8v-2h-2v6h2zM7 9v2H3v2h4v2h2V9H7zm14 " +
                            "4v-2H11v2h10zm-6-4h2V7h4V5h-4V3h-2v6z",
                    ).toNodes(),
            fill = SolidColor(Color.Black),
        ).build()
}

internal object FilterIcons
