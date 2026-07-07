package com.budgetpilot.core.designsystem.icons

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

/**
 * Category glyphs not present in `material-icons-core` (DESIGN-SPEC.md §1.3),
 * copied from the MIT-licensed `@material-design-icons/svg` "filled" set
 * rather than adding the `material-icons-extended` dependency.
 */
object CategoryIcons {
    val Restaurant: ImageVector by lazy {
        vectorOf(
            name = "Restaurant",
            path =
                "M11 9H9V2H7v7H5V2H3v7c0 2.12 1.66 3.84 3.75 3.97V22h2.5v-9.03C11.34 12.84 13 " +
                    "11.12 13 9V2h-2v7zm5-3v8h2.5v8H21V2c-2.76 0-5 2.24-5 4z",
        )
    }

    val DirectionsBus: ImageVector by lazy {
        vectorOf(
            name = "DirectionsBus",
            path =
                "M4 16c0 .88.39 1.67 1 2.22V20c0 .55.45 1 1 1h1c.55 0 1-.45 1-1v-1h8v1c0 " +
                    ".55.45 1 1 1h1c.55 0 1-.45 1-1v-1.78c.61-.55 1-1.34 1-2.22V6c0-3.5-3.58-4-8-4s-8 " +
                    ".5-8 4v10zm3.5 1c-.83 0-1.5-.67-1.5-1.5S6.67 14 7.5 14s1.5.67 1.5 1.5S8.33 " +
                    "17 7.5 17zm9 0c-.83 0-1.5-.67-1.5-1.5s.67-1.5 1.5-1.5 1.5.67 1.5 1.5-.67 " +
                    "1.5-1.5 1.5zm1.5-6H6V6h12v5z",
        )
    }

    val Bolt: ImageVector by lazy {
        vectorOf(
            name = "Bolt",
            path =
                "M11 21h-1l1-7H7.5c-.58 0-.57-.32-.38-.66.19-.34.05-.08.07-.12C8.48 10.94 " +
                    "10.42 7.54 13 3h1l-1 7h3.5c.49 0 .56.33.47.51l-.07.15C12.96 17.55 11 21 11 21z",
        )
    }

    val ShoppingBag: ImageVector by lazy {
        vectorOf(
            name = "ShoppingBag",
            path =
                "M18 6h-2c0-2.21-1.79-4-4-4S8 3.79 8 6H6c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h12c" +
                    "1.1 0 2-.9 2-2V8c0-1.1-.9-2-2-2zm-6-2c1.1 0 2 .9 2 2h-4c0-1.1.9-2 2-2zm0 " +
                    "6c-2.76 0-5-2.24-5-5h2c0 1.66 1.34 3 3 3s3-1.34 3-3h2c0 2.76-2.24 5-5 5z",
        )
    }

    val Movie: ImageVector by lazy {
        vectorOf(
            name = "Movie",
            path =
                "m18 4 2 4h-3l-2-4h-2l2 4h-3l-2-4H8l2 4H7L5 4H4c-1.1 0-1.99.9-1.99 2L2 18c0 " +
                    "1.1.9 2 2 2h16c1.1 0 2-.9 2-2V4h-4z",
        )
    }

    val Category: ImageVector by lazy {
        vectorOf(
            name = "Category",
            paths =
                listOf(
                    "m12 2-5.5 9h11z",
                    "M17.5 13a4.5 4.5 0 1 0 0 9 4.5 4.5 0 0 0 0-9z",
                    "M3 13.5h8v8H3z",
                ),
        )
    }
}

/** Maps a seeded category's `iconKey` to its glyph; unrecognized keys fall back to [CategoryIcons.Category]. */
fun categoryIcon(iconKey: String): ImageVector =
    when (iconKey) {
        "restaurant" -> CategoryIcons.Restaurant
        "directions_bus" -> CategoryIcons.DirectionsBus
        "bolt" -> CategoryIcons.Bolt
        "shopping_cart" -> Icons.Filled.ShoppingCart
        "shopping_bag" -> CategoryIcons.ShoppingBag
        "favorite" -> Icons.Filled.Favorite
        "movie" -> CategoryIcons.Movie
        else -> CategoryIcons.Category
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
