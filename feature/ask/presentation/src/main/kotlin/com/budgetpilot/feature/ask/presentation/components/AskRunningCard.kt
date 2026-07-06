package com.budgetpilot.feature.ask.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.budgetpilot.core.designsystem.components.AppCard
import com.budgetpilot.core.designsystem.components.LoadingSkeleton
import com.budgetpilot.core.designsystem.icons.StateIcons
import com.budgetpilot.core.designsystem.theme.BudgetPilotTheme
import com.budgetpilot.core.designsystem.theme.Spacing
import com.budgetpilot.feature.ask.presentation.AskStagedStatus
import com.budgetpilot.feature.ask.presentation.AskTraceStepUi
import com.budgetpilot.feature.ask.presentation.R

private val SkeletonLineHeight = 16.dp

/**
 * In-flight answer: staged status line (announced to screen readers as it advances),
 * trace rows for tools that have already completed, and skeleton lines holding the
 * final answer's place (DESIGN-SPEC.md §10).
 */
@Composable
fun AskRunningCard(
    stagedStatus: AskStagedStatus,
    trace: List<AskTraceStepUi>,
    modifier: Modifier = Modifier,
) {
    AppCard(modifier = modifier.fillMaxWidth()) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(Spacing.extraSmall),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
        ) {
            Icon(
                imageVector = StateIcons.Sparkle,
                contentDescription = stringResource(R.string.cd_sparkle_icon),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
            )
            Text(
                text = stagedStatus.label(),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        if (trace.isNotEmpty()) {
            Column {
                Spacer(Modifier.height(Spacing.small))
                trace.forEachIndexed { index, step ->
                    TraceStepRow(
                        stepNumber = index + 1,
                        toolName = step.toolName,
                        argsSummary = step.argsSummary,
                        resultSummary = step.resultSummary,
                        durationMs = step.durationMs,
                        modifier = Modifier.padding(top = if (index == 0) Spacing.none else Spacing.small),
                    )
                }
            }
        }
        Column(modifier = Modifier.padding(top = Spacing.small)) {
            LoadingSkeleton(
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.fillMaxWidth().height(SkeletonLineHeight),
            )
            Spacer(Modifier.height(Spacing.extraSmall))
            LoadingSkeleton(
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.fillMaxWidth(SKELETON_SECOND_LINE_WIDTH_FRACTION).height(SkeletonLineHeight),
            )
        }
    }
}

private const val SKELETON_SECOND_LINE_WIDTH_FRACTION = 0.6f

@Composable
private fun AskStagedStatus.label(): String =
    stringResource(
        when (this) {
            AskStagedStatus.PLANNING -> R.string.status_planning
            AskStagedStatus.CHECKING_EXPENSES -> R.string.status_checking_expenses
            AskStagedStatus.ALMOST_DONE -> R.string.status_almost_done
        },
    )

@PreviewLightDark
@Composable
private fun AskRunningCardPreview() {
    BudgetPilotTheme {
        Surface {
            AskRunningCard(
                stagedStatus = AskStagedStatus.CHECKING_EXPENSES,
                trace =
                    listOf(
                        AskTraceStepUi("resolve_date_range", "\"this month\"", "Jun 1–30", 210),
                    ),
            )
        }
    }
}
