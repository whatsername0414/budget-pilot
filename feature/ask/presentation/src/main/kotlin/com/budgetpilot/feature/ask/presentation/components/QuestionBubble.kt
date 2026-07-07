package com.budgetpilot.feature.ask.presentation.components

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.budgetpilot.core.designsystem.theme.BudgetPilotTheme

private val QuestionBubbleShape =
    RoundedCornerShape(
        topStart = 18.dp,
        topEnd = 18.dp,
        bottomEnd = 4.dp,
        bottomStart = 18.dp,
    )
private const val MAX_WIDTH_FRACTION = 0.78f
private val BubbleHorizontalPadding = 14.dp
private val BubbleVerticalPadding = 10.dp

/** The user's own question: right-aligned, shrink-wrapped up to ~78% width (DESIGN-SPEC.md §10). */
@Composable
fun QuestionBubble(
    question: String,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        Surface(
            modifier = Modifier.align(Alignment.CenterEnd).widthIn(max = maxWidth * MAX_WIDTH_FRACTION),
            shape = QuestionBubbleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
        ) {
            Text(
                text = question,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(horizontal = BubbleHorizontalPadding, vertical = BubbleVerticalPadding),
            )
        }
    }
}

@Preview
@Composable
private fun QuestionBubblePreview() {
    BudgetPilotTheme {
        Surface {
            QuestionBubble(question = "How much did I spend on food last month?")
        }
    }
}
