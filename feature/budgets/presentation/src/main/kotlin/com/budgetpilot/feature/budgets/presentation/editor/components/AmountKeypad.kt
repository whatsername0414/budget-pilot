package com.budgetpilot.feature.budgets.presentation.editor.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.budgetpilot.core.designsystem.theme.BudgetPilotTheme
import com.budgetpilot.core.designsystem.theme.Spacing
import kotlinx.coroutines.delay

private val KeypadRows =
    listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf(".", "0", AmountKeypadKeys.BACKSPACE),
    )

private val KeyHeight = 56.dp
private val KeyShape = RoundedCornerShape(12.dp)
private const val BACKSPACE_INITIAL_DELAY_MS = 400L
private const val BACKSPACE_REPEAT_DELAY_MS = 80L

/**
 * DESIGN-SPEC.md §14: same 4x3 plain-decimal-entry keypad as
 * `:feature:expenses`'s `AmountKeypad`, duplicated here rather than shared —
 * features never depend on each other (PLAN.md §3).
 */
@Composable
fun AmountKeypad(
    onKeyPress: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.small),
    ) {
        KeypadRows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.small),
            ) {
                row.forEach { key ->
                    AmountKey(
                        key = key,
                        onPress = { onKeyPress(key) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun AmountKey(
    key: String,
    onPress: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isRepeatable = key == AmountKeypadKeys.BACKSPACE
    val currentOnPress by rememberUpdatedState(onPress)

    if (isRepeatable) {
        LaunchedEffect(isPressed) {
            if (!isPressed) return@LaunchedEffect
            delay(BACKSPACE_INITIAL_DELAY_MS)
            while (isPressed) {
                currentOnPress()
                delay(BACKSPACE_REPEAT_DELAY_MS)
            }
        }
    }

    Surface(
        modifier =
            modifier
                .height(KeyHeight)
                .clickable(
                    interactionSource = interactionSource,
                    onClick = onPress,
                ),
        shape = KeyShape,
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = if (key == AmountKeypadKeys.BACKSPACE) "⌫" else key,
                style = MaterialTheme.typography.headlineSmall,
            )
        }
    }
}

@Preview
@Composable
private fun AmountKeypadPreview() {
    BudgetPilotTheme {
        Surface {
            AmountKeypad(onKeyPress = {})
        }
    }
}
