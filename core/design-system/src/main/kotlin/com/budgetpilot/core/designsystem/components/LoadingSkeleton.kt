package com.budgetpilot.core.designsystem.components

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.budgetpilot.core.designsystem.R
import com.budgetpilot.core.designsystem.theme.BudgetPilotTheme
import com.budgetpilot.core.designsystem.theme.Spacing

@Suppress("ktlint:compose:modifier-composed-check", "ModifierComposed")
fun Modifier.shimmerEffect(): Modifier =
    composed {
        val baseColor = MaterialTheme.colorScheme.surfaceContainerHigh

        if (BudgetPilotTheme.reducedMotionEnabled) {
            background(baseColor)
        } else {
            var size by remember { mutableStateOf(IntSize.Zero) }
            val transition = rememberInfiniteTransition(label = "shimmer")
            val startOffsetX by transition.animateFloat(
                initialValue = -2 * size.width.toFloat(),
                targetValue = 2 * size.width.toFloat(),
                animationSpec = infiniteRepeatable(animation = tween(durationMillis = 1000)),
                label = "shimmerTranslate",
            )
            val highlightColor = MaterialTheme.colorScheme.surfaceContainerHighest

            background(
                brush =
                    Brush.linearGradient(
                        colors = listOf(baseColor, highlightColor, baseColor),
                        start = Offset(startOffsetX, 0f),
                        end = Offset(startOffsetX + size.width.toFloat(), size.height.toFloat()),
                    ),
            ).onGloballyPositioned { size = it.size }
        }
    }

/** Shimmering placeholder shown while content loads. */
@Composable
fun LoadingSkeleton(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(8.dp),
) {
    val loadingDescription = stringResource(R.string.cd_loading)
    Box(
        modifier =
            modifier
                .clip(shape)
                .shimmerEffect()
                .semantics { contentDescription = loadingDescription },
    )
}

@Preview
@Composable
private fun LoadingSkeletonPreview() {
    BudgetPilotTheme {
        Surface {
            Column(modifier = Modifier.padding(Spacing.medium)) {
                LoadingSkeleton(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(20.dp),
                )
                Spacer(Modifier.height(Spacing.small))
                LoadingSkeleton(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(64.dp),
                )
            }
        }
    }
}
