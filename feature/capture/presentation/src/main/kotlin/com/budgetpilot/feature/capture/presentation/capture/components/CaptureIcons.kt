package com.budgetpilot.feature.capture.presentation.capture.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

/**
 * None of these are in `material-icons-core`; copied from the MIT-licensed
 * `@material-design-icons/svg` "filled" set (same approach as
 * `:core:design-system`'s `CategoryIcons`) rather than adding
 * `material-icons-extended`.
 */
internal object CaptureIcons {
    val FlashOn: ImageVector by lazy { singlePathIcon("FlashOn", "M7 2v11h3v9l7-12h-4l4-8z") }

    val FlashOff: ImageVector by lazy {
        singlePathIcon(
            "FlashOff",
            "M3.27 3 2 4.27l5 5V13h3v9l3.58-6.14L17.73 20 19 18.73 3.27 3zM17 10h-4l4-8H7v2.18l8.46 8.46L17 10z",
        )
    }

    val PhotoLibrary: ImageVector by lazy {
        singlePathIcon(
            "PhotoLibrary",
            "M22 16V4c0-1.1-.9-2-2-2H8c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2zm-11-4 " +
                "2.03 2.71L16 11l4 5H8l3-4zM2 6v14c0 1.1.9 2 2 2h14v-2H4V6H2z",
        )
    }

    // Camera lens ring (radius 5) + aperture dot (radius 3.2), the two shapes
    // `photo_camera`'s original SVG expresses as separate <path>/<circle> elements.
    val PhotoCamera: ImageVector by lazy {
        ImageVector
            .Builder(name = "PhotoCamera", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f)
            .addPath(
                pathData =
                    PathParser()
                        .parsePathString(
                            "M9 2 7.17 4H4c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2h-3.17L15 2H9zm3 " +
                                "15c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5z",
                        ).toNodes(),
                fill = SolidColor(Color.Black),
            ).addPath(
                pathData = PathParser().parsePathString("M8.8 12a3.2 3.2 0 1 0 6.4 0a3.2 3.2 0 1 0 -6.4 0z").toNodes(),
                fill = SolidColor(Color.Black),
            ).build()
    }

    private fun singlePathIcon(
        iconName: String,
        pathData: String,
    ): ImageVector =
        ImageVector
            .Builder(name = iconName, defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f)
            .addPath(pathData = PathParser().parsePathString(pathData).toNodes(), fill = SolidColor(Color.Black))
            .build()
}
