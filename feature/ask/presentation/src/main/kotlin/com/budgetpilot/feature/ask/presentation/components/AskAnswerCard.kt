package com.budgetpilot.feature.ask.presentation.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import com.budgetpilot.core.designsystem.components.AppCard
import com.budgetpilot.core.designsystem.theme.BudgetPilotTheme
import com.budgetpilot.core.designsystem.theme.Spacing
import com.budgetpilot.feature.ask.presentation.AskTraceStepUi
import com.budgetpilot.feature.ask.presentation.AskTurn
import com.budgetpilot.feature.ask.presentation.R
import kotlin.math.roundToInt

private val PesoAmountRegex = Regex("""₱[\d,]+(\.\d+)?""")

/** Completed answer: text, data-used line, and the collapsible reasoning trace (DESIGN-SPEC.md §10). */
@Composable
fun AskAnswerCard(
    turn: AskTurn,
    onToggleTraceExpand: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AppCard(modifier = modifier.fillMaxWidth().animateContentSize()) {
        Text(text = answerWithBoldAmounts(turn.answerText), style = MaterialTheme.typography.bodyLarge)
        if (turn.dataUsedSummary != null) {
            Text(
                text = turn.dataUsedSummary,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = Spacing.extraSmall),
            )
        }
        HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.small))
        TraceExpanderHeader(isExpanded = turn.isTraceExpanded, onClick = onToggleTraceExpand)
        if (turn.isTraceExpanded) {
            Column(
                modifier = Modifier.padding(top = Spacing.small),
                verticalArrangement = Arrangement.spacedBy(Spacing.small),
            ) {
                turn.trace.forEachIndexed { index, step ->
                    TraceStepRow(
                        stepNumber = index + 1,
                        toolName = step.toolName,
                        argsSummary = step.argsSummary,
                        resultSummary = step.resultSummary,
                        durationMs = step.durationMs,
                    )
                }
                Text(
                    text =
                        pluralStringResource(
                            R.plurals.answer_composed_summary,
                            turn.modelTurnCount,
                            turn.modelTurnCount,
                            formatTotalDuration(turn.totalDurationMs),
                        ),
                    style = MaterialTheme.typography.labelSmall.copy(fontFeatureSettings = "tnum"),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun TraceExpanderHeader(
    isExpanded: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.how_i_calculated_this),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        Icon(
            imageVector = if (isExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
            contentDescription =
                stringResource(if (isExpanded) R.string.cd_collapse_trace else R.string.cd_expand_trace),
            tint = MaterialTheme.colorScheme.primary,
        )
    }
}

private fun answerWithBoldAmounts(text: String) =
    buildAnnotatedString {
        var cursor = 0
        for (match in PesoAmountRegex.findAll(text)) {
            append(text.substring(cursor, match.range.first))
            withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontFeatureSettings = "tnum")) {
                append(match.value)
            }
            cursor = match.range.last + 1
        }
        append(text.substring(cursor))
    }

private fun formatTotalDuration(totalDurationMs: Long): String {
    val tenthsOfSecond = (totalDurationMs / MS_PER_TENTH_SECOND.toDouble()).roundToInt()
    return "${tenthsOfSecond / TENTHS_PER_SECOND}.${tenthsOfSecond % TENTHS_PER_SECOND}s"
}

private const val MS_PER_TENTH_SECOND = 100
private const val TENTHS_PER_SECOND = 10

@Preview
@Composable
private fun AskAnswerCardPreview() {
    BudgetPilotTheme {
        Surface {
            AskAnswerCard(
                turn =
                    AskTurn(
                        id = 1L,
                        question = "How much did I spend on food this month?",
                        answerText = "You've spent ₱5,872.25 on Food this month, across 23 expenses.",
                        dataUsedSummary = "Based on 23 Food expenses, Jun 1–30.",
                        trace =
                            listOf(
                                AskTraceStepUi("resolve_date_range", "\"this month\"", "Jun 1–30", 210),
                                AskTraceStepUi(
                                    "query_expenses",
                                    "category: Food, Jun 1–30",
                                    "23 expenses, ₱5,872.25",
                                    1240,
                                ),
                            ),
                        isTraceExpanded = true,
                        modelTurnCount = 2,
                        totalDurationMs = 4100,
                    ),
                onToggleTraceExpand = {},
            )
        }
    }
}
