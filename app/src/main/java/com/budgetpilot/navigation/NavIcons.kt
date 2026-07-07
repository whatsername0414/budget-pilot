package com.budgetpilot.navigation

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

/**
 * Bottom-nav glyphs from the approved mockup (artifact 4d800ab6): History
 * uses a receipt, Ask a chat bubble, Budgets a wallet — none in
 * `material-icons-core`. Filled paths are copied verbatim from the mockup's
 * own icon sprite; outlined counterparts are copied from the matching
 * MIT-licensed `@material-design-icons/svg` "outlined" glyph (same source
 * family already used for `CategoryIcons`/`FilterIcons`) rather than adding
 * `material-icons-extended`. [CameraFilled] (2026-07-07 FAB speed-dial
 * rework) is the same MIT "photo_camera" glyph `:feature:capture:presentation`
 * already carries as its private `CaptureIcons.PhotoCamera` — duplicated here
 * since that one isn't exported and `:app` can't depend on it.
 */
object NavIcons {
    val ReceiptFilled: ImageVector by lazy {
        vectorOf(
            name = "ReceiptFilled",
            path =
                "M18 17H6v-2h12v2zm0-4H6v-2h12v2zm0-4H6V7h12v2zM3 22l1.5-1.5L6 22l1.5-1.5L9 " +
                    "22l1.5-1.5L12 22l1.5-1.5L15 22l1.5-1.5L18 22l1.5-1.5L21 22V2l-1.5 1.5L18 2l-1.5 " +
                    "1.5L15 2l-1.5 1.5L12 2l-1.5 1.5L9 2 7.5 3.5 6 2 4.5 3.5 3 2v20z",
        )
    }

    val ReceiptOutlined: ImageVector by lazy {
        vectorOf(
            name = "ReceiptOutlined",
            path =
                "M19.5 3.5 18 2l-1.5 1.5L15 2l-1.5 1.5L12 2l-1.5 1.5L9 2 7.5 3.5 6 2 4.5 3.5 3 " +
                    "2v20l1.5-1.5L6 22l1.5-1.5L9 22l1.5-1.5L12 22l1.5-1.5L15 22l1.5-1.5L18 22l1.5-1.5L21 " +
                    "22V2l-1.5 1.5zM19 19.09H5V4.91h14v14.18zM6 15h12v2H6zm0-4h12v2H6zm0-4h12v2H6z",
        )
    }

    val ChatFilled: ImageVector by lazy {
        vectorOf(
            name = "ChatFilled",
            path = "M20 2H4c-1.1 0-2 .9-2 2v18l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2z",
        )
    }

    val ChatOutlined: ImageVector by lazy {
        vectorOf(
            name = "ChatOutlined",
            path =
                "M4 4h16v12H5.17L4 17.17V4m0-2c-1.1 0-1.99.9-1.99 2L2 22l4-4h14c1.1 0 2-.9 " +
                    "2-2V4c0-1.1-.9-2-2-2H4zm2 10h8v2H6v-2zm0-3h12v2H6V9zm0-3h12v2H6V6z",
        )
    }

    val WalletFilled: ImageVector by lazy {
        vectorOf(
            name = "WalletFilled",
            path =
                "M21 18v1c0 1.1-.9 2-2 2H5c-1.11 0-2-.9-2-2V5c0-1.1.89-2 2-2h14c1.1 0 2 .9 2 " +
                    "2v1h-9c-1.11 0-2 .9-2 2v8c0 1.1.89 2 2 2h9zm-9-2h10V8H12v8zm4-2.5c-.83 0-1.5-.67-1.5-1.5s.67-1.5 " +
                    "1.5-1.5 1.5.67 1.5 1.5-.67 1.5-1.5 1.5z",
        )
    }

    val WalletOutlined: ImageVector by lazy {
        vectorOf(
            name = "WalletOutlined",
            paths =
                listOf(
                    "M21 7.28V5c0-1.1-.9-2-2-2H5a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14c1.1 0 2-.9 " +
                        "2-2v-2.28A2 2 0 0 0 22 15V9a2 2 0 0 0-1-1.72zM20 9v6h-7V9h7zM5 19V5h14v2h-6c-1.1 " +
                        "0-2 .9-2 2v6c0 1.1.9 2 2 2h6v2H5z",
                    "M17.5,12A1.5,1.5,0,1,1,14.5,12A1.5,1.5,0,1,1,17.5,12Z",
                ),
        )
    }

    val CameraFilled: ImageVector by lazy {
        vectorOf(
            name = "CameraFilled",
            paths =
                listOf(
                    "M9 2 7.17 4H4c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2h-3.17L15 " +
                        "2H9zm3 15c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5z",
                    "M8.8 12a3.2 3.2 0 1 0 6.4 0a3.2 3.2 0 1 0 -6.4 0z",
                ),
        )
    }
}

private fun vectorOf(
    name: String,
    path: String,
): ImageVector = vectorOf(name, listOf(path))

private fun vectorOf(
    name: String,
    paths: List<String>,
): ImageVector =
    ImageVector
        .Builder(
            name = name,
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            paths.forEach { path ->
                addPath(
                    pathData = PathParser().parsePathString(path).toNodes(),
                    fill = SolidColor(Color.Black),
                )
            }
        }.build()
