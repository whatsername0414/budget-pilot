package com.budgetpilot.feature.capture.presentation.confirm.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.budgetpilot.core.designsystem.theme.BudgetPilotTheme
import com.budgetpilot.feature.capture.domain.model.Confidence

private val ConfidenceBorderWidth = 2.dp

/** Rarely-varied [OutlinedTextField] knobs, grouped to keep [ConfidenceField]'s own parameter list short. */
data class ConfidenceFieldOptions(
    val readOnly: Boolean = false,
    val textStyle: TextStyle? = null,
    val keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    val interactionSource: MutableInteractionSource? = null,
)

/**
 * Outlined field that highlights MEDIUM/LOW-confidence extracted values with a
 * warning border/tint + glyph + helper text (DESIGN-SPEC.md §9); HIGH confidence
 * renders as a normal field with a tertiary check icon.
 */
@Composable
fun ConfidenceField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    confidence: Confidence,
    lowConfidenceHelperText: String,
    modifier: Modifier = Modifier,
    options: ConfidenceFieldOptions = ConfidenceFieldOptions(),
) {
    val isLowConfidence = confidence != Confidence.HIGH
    val warning = BudgetPilotTheme.extendedColors.warning
    val shape = OutlinedTextFieldDefaults.shape

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier =
            modifier
                .fillMaxWidth()
                .let { if (isLowConfidence) it.border(BorderStroke(ConfidenceBorderWidth, warning), shape) else it },
        label = { Text(label) },
        readOnly = options.readOnly,
        singleLine = true,
        textStyle = options.textStyle ?: LocalTextStyle.current,
        keyboardOptions = options.keyboardOptions,
        interactionSource = options.interactionSource ?: remember { MutableInteractionSource() },
        shape = shape,
        trailingIcon = {
            Icon(
                imageVector = if (isLowConfidence) Icons.Filled.Warning else Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = if (isLowConfidence) warning else MaterialTheme.colorScheme.tertiary,
            )
        },
        supportingText =
            if (isLowConfidence) {
                { Text(text = lowConfidenceHelperText, color = warning) }
            } else {
                null
            },
        colors =
            if (isLowConfidence) {
                OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = warning,
                    focusedBorderColor = warning,
                    unfocusedContainerColor = BudgetPilotTheme.extendedColors.warningContainer.copy(alpha = 0.10f),
                    focusedContainerColor = BudgetPilotTheme.extendedColors.warningContainer.copy(alpha = 0.10f),
                    unfocusedLabelColor = warning,
                    focusedLabelColor = warning,
                )
            } else {
                OutlinedTextFieldDefaults.colors()
            },
    )
}
