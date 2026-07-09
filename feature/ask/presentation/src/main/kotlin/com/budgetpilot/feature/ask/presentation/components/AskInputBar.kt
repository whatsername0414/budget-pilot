package com.budgetpilot.feature.ask.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.budgetpilot.core.designsystem.theme.BudgetPilotTheme
import com.budgetpilot.core.designsystem.theme.Spacing
import com.budgetpilot.feature.ask.presentation.R

private val InputFieldShape = RoundedCornerShape(26.dp)
private val SendButtonSize = 48.dp

/** Pinned question input + circular send button, both disabled during a run (DESIGN-SPEC.md §10). */
@Composable
fun AskInputBar(
    questionInput: String,
    isSending: Boolean,
    onQuestionChange: (String) -> Unit,
    onSendClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Scaffold's bottomBar slot is placed flush with the screen edge regardless of the
    // keyboard (only its own contentWindowInsets affects the *content* padding) — this
    // is what actually pushes the bar above the IME.
    Surface(modifier = modifier.fillMaxWidth().imePadding(), color = MaterialTheme.colorScheme.background) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(Spacing.medium),
            horizontalArrangement = Arrangement.spacedBy(Spacing.small),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = questionInput,
                onValueChange = onQuestionChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text(stringResource(R.string.ask_input_placeholder)) },
                enabled = !isSending,
                singleLine = true,
                shape = InputFieldShape,
                colors =
                    OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        disabledContainerColor = MaterialTheme.colorScheme.surface,
                    ),
            )
            FilledIconButton(
                onClick = onSendClick,
                enabled = !isSending && questionInput.isNotBlank(),
                modifier = Modifier.size(SendButtonSize),
                colors = IconButtonDefaults.filledIconButtonColors(),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = stringResource(R.string.cd_send),
                )
            }
        }
    }
}

@Preview
@Composable
private fun AskInputBarPreview() {
    BudgetPilotTheme {
        AskInputBar(questionInput = "How much on food?", isSending = false, onQuestionChange = {}, onSendClick = {})
    }
}
