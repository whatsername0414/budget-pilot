package com.budgetpilot.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Corner-radius scale used for cards, chips, and other containers across the app. */
object Shapes {
    val extraSmall: Shape = RoundedCornerShape(4.dp)
    val small: Shape = RoundedCornerShape(6.dp)
    val medium: Shape = RoundedCornerShape(12.dp)
    val large: Shape = RoundedCornerShape(16.dp)
    val extraLarge: Shape = RoundedCornerShape(24.dp)
    val pill: Shape = RoundedCornerShape(percent = 50)
}

/** Hairline border width used for card/container outlines across the app. */
val HairlineBorderWidth: Dp = 1.dp
