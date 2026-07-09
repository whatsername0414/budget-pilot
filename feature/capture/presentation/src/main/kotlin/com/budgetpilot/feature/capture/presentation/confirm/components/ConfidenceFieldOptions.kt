package com.budgetpilot.feature.capture.presentation.confirm.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.text.TextStyle

/** Rarely-varied [OutlinedTextField] knobs, grouped to keep [ConfidenceField]'s own parameter list short. */
data class ConfidenceFieldOptions(
    val readOnly: Boolean = false,
    val textStyle: TextStyle? = null,
    val keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    val interactionSource: MutableInteractionSource? = null,
)
