package com.budgetpilot.feature.ask.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.budgetpilot.core.designsystem.theme.BudgetPilotTheme
import com.budgetpilot.core.designsystem.theme.Shapes
import com.budgetpilot.core.designsystem.theme.Spacing
import com.budgetpilot.feature.ask.presentation.R
import kotlin.math.roundToInt

private val StepDotSize = 22.dp
private val CompletedDotIconSize = 13.dp
private val ToolNamePillHorizontalPadding = 5.dp
private val ToolNamePillVerticalPadding = 1.dp
private const val TOOL_NAME_PILL_BACKGROUND_ALPHA = 0.10f

/**
 * One row of the "How I calculated this" reasoning trace: a numbered/checked dot,
 * the tool name in code style, humanized args -> result, and duration
 * (DESIGN-SPEC.md §10). [AgentLoop.onStep] only fires once a tool call finishes, so
 * every rendered row is inherently completed — [isCompleted] exists for the row's
 * own visual contract rather than a currently-reachable pending state.
 */
@Composable
fun TraceStepRow(
    stepNumber: Int,
    toolName: String,
    argsSummary: String,
    resultSummary: String,
    durationMs: Long,
    modifier: Modifier = Modifier,
    isCompleted: Boolean = true,
) {
    val durationLabel = formatDurationLabel(durationMs)
    val description =
        stringResource(
            R.string.cd_trace_step,
            stepNumber,
            toolName,
            argsSummary,
            resultSummary,
            durationLabel,
        )
    Row(
        modifier = modifier.fillMaxWidth().semantics { contentDescription = description },
        horizontalArrangement = Arrangement.spacedBy(Spacing.small),
    ) {
        StepDot(stepNumber = stepNumber, isCompleted = isCompleted)
        Column(modifier = Modifier.weight(1f)) {
            val pillBackground = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = TOOL_NAME_PILL_BACKGROUND_ALPHA)
            Text(
                text = toolName,
                style =
                    MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                    ),
                modifier =
                    Modifier
                        .background(color = pillBackground, shape = Shapes.extraSmall)
                        .padding(horizontal = ToolNamePillHorizontalPadding, vertical = ToolNamePillVerticalPadding),
            )
            Text(
                text = "$argsSummary → $resultSummary",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = durationLabel,
            style = MaterialTheme.typography.labelSmall.copy(fontFeatureSettings = "tnum"),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun StepDot(
    stepNumber: Int,
    isCompleted: Boolean,
) {
    Surface(
        shape = CircleShape,
        color =
            if (isCompleted) {
                MaterialTheme.colorScheme.tertiaryContainer
            } else {
                MaterialTheme.colorScheme.secondaryContainer
            },
        modifier = Modifier.size(StepDotSize),
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (isCompleted) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.size(CompletedDotIconSize),
                )
            } else {
                Text(
                    text = stepNumber.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
        }
    }
}

private fun formatDurationLabel(durationMs: Long): String {
    val tenthsOfSecond = (durationMs / MS_PER_TENTH_SECOND.toDouble()).roundToInt()
    return "${tenthsOfSecond / TENTHS_PER_SECOND}.${tenthsOfSecond % TENTHS_PER_SECOND}s"
}

private const val MS_PER_TENTH_SECOND = 100
private const val TENTHS_PER_SECOND = 10

@Preview
@Composable
private fun TraceStepRowPreview() {
    BudgetPilotTheme {
        Surface {
            Column(modifier = Modifier.fillMaxWidth()) {
                TraceStepRow(
                    stepNumber = 1,
                    toolName = "resolve_date_range",
                    argsSummary = "\"last month\"",
                    resultSummary = "Jun 1–30",
                    durationMs = 210,
                )
                TraceStepRow(
                    stepNumber = 2,
                    toolName = "query_expenses",
                    argsSummary = "category: Food, Jun 1–30",
                    resultSummary = "23 expenses, ₱5,872.25",
                    durationMs = 1240,
                )
            }
        }
    }
}
