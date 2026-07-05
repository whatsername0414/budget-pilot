package com.budgetpilot.feature.budgets.presentation.main.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private const val DASH_LENGTH_PX = 12f
private const val DASH_GAP_PX = 8f
private val DashInterval = floatArrayOf(DASH_LENGTH_PX, DASH_GAP_PX)

/** Dashed outline for unbudgeted category rows (DESIGN-SPEC.md §6). */
fun Modifier.dashedBorder(
    color: Color,
    shape: Shape = RoundedCornerShape(16.dp),
    strokeWidth: Dp = 1.dp,
): Modifier =
    drawWithContent {
        drawContent()
        val outline = shape.createOutline(size, layoutDirection, this)
        drawOutline(
            outline = outline,
            color = color,
            style = Stroke(width = strokeWidth.toPx(), pathEffect = PathEffect.dashPathEffect(DashInterval, 0f)),
        )
    }
