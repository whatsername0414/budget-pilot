package com.budgetpilot.feature.capture.presentation.confirm

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.budgetpilot.core.designsystem.components.CategoryChip
import com.budgetpilot.core.designsystem.icons.categoryIcon
import com.budgetpilot.core.designsystem.theme.BudgetPilotTheme
import com.budgetpilot.core.designsystem.theme.Spacing
import com.budgetpilot.core.designsystem.theme.categoryColor
import com.budgetpilot.core.domain.model.Category
import com.budgetpilot.feature.capture.domain.model.Confidence
import com.budgetpilot.feature.capture.presentation.R
import com.budgetpilot.feature.capture.presentation.confirm.components.ConfidenceField
import com.budgetpilot.feature.capture.presentation.confirm.components.ConfidenceFieldOptions
import com.budgetpilot.feature.capture.presentation.confirm.components.LineItemsCard
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun ConfirmExpenseLoadedContent(
    state: ConfirmExpenseState,
    onAction: (ConfirmExpenseAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isDatePickerVisible by remember { mutableStateOf(false) }
    val hasLowConfidenceFields =
        listOf(state.merchantConfidence, state.dateConfidence, state.amountConfidence, state.categoryConfidence)
            .any { it != Confidence.HIGH }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(Spacing.medium),
        verticalArrangement = Arrangement.spacedBy(Spacing.medium),
    ) {
        ThumbnailRow(
            imagePath = state.imagePath,
            hasLowConfidenceFields = hasLowConfidenceFields,
            onThumbnailClick = { onAction(ConfirmExpenseAction.OnThumbnailClick) },
        )

        ConfirmExpenseFields(
            state = state,
            onAction = onAction,
            onDateFieldClick = { isDatePickerVisible = true },
        )

        CategorySuggestionSection(
            categories = state.categories,
            selectedCategoryId = state.selectedCategoryId,
            confidence = state.categoryConfidence,
            isSuggested = !state.isCategoryManuallySelected,
            onCategorySelect = { onAction(ConfirmExpenseAction.OnCategorySelect(it)) },
        )

        OutlinedTextField(
            value = state.note,
            onValueChange = { onAction(ConfirmExpenseAction.OnNoteChange(it)) },
            label = { Text(stringResource(R.string.label_note_optional)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )

        ActionsRow(
            isValid = state.isValid,
            isSaving = state.isSaving,
            onRetakeClick = { onAction(ConfirmExpenseAction.OnRetakeClick) },
            onSaveClick = { onAction(ConfirmExpenseAction.OnSaveClick) },
        )
    }

    if (isDatePickerVisible) {
        ConfirmExpenseDatePickerDialog(
            initialDate = state.date,
            onDateSelect = {
                onAction(ConfirmExpenseAction.OnDateSelect(it))
                isDatePickerVisible = false
            },
            onDismiss = { isDatePickerVisible = false },
        )
    }
}

@Composable
private fun ConfirmExpenseFields(
    state: ConfirmExpenseState,
    onAction: (ConfirmExpenseAction) -> Unit,
    onDateFieldClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Spacing.medium)) {
        ConfidenceField(
            value = state.merchant,
            onValueChange = { onAction(ConfirmExpenseAction.OnMerchantChange(it)) },
            label = stringResource(if (state.isGCashOrMaya) R.string.label_sent_to else R.string.label_merchant),
            confidence = state.merchantConfidence,
            lowConfidenceHelperText = stringResource(R.string.confidence_helper_merchant),
        )

        ConfidenceField(
            value = state.date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)),
            onValueChange = {},
            label = stringResource(R.string.label_date),
            confidence = state.dateConfidence,
            lowConfidenceHelperText = stringResource(R.string.confidence_helper_date),
            options =
                ConfidenceFieldOptions(
                    readOnly = true,
                    interactionSource = rememberClickInteractionSource(onDateFieldClick),
                ),
        )

        ConfidenceField(
            value = state.amountText,
            onValueChange = { onAction(ConfirmExpenseAction.OnAmountChange(it)) },
            label = stringResource(R.string.label_total),
            confidence = state.amountConfidence,
            lowConfidenceHelperText = stringResource(R.string.confidence_helper_amount),
            options =
                ConfidenceFieldOptions(
                    textStyle = MaterialTheme.typography.titleLarge.copy(fontFeatureSettings = "tnum"),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                ),
        )

        if (!state.isGCashOrMaya && state.lineItems.isNotEmpty()) {
            LineItemsCard(
                lineItems = state.lineItems,
                isExpanded = state.isLineItemsExpanded,
                onToggleClick = { onAction(ConfirmExpenseAction.OnLineItemsToggleClick) },
            )
        }
    }
}

@Composable
private fun rememberClickInteractionSource(onClick: () -> Unit): MutableInteractionSource {
    val interactionSource = remember { MutableInteractionSource() }
    val currentOnClick by rememberUpdatedState(onClick)
    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            if (interaction is PressInteraction.Release) currentOnClick()
        }
    }
    return interactionSource
}

@Composable
private fun ThumbnailRow(
    imagePath: String,
    hasLowConfidenceFields: Boolean,
    onThumbnailClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.medium),
    ) {
        AsyncImage(
            model = File(imagePath),
            contentDescription = stringResource(R.string.cd_receipt_photo),
            modifier =
                Modifier
                    .size(width = 56.dp, height = 72.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onThumbnailClick),
            contentScale = ContentScale.Crop,
        )
        Column {
            Text(
                text = stringResource(R.string.thumbnail_caption),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (hasLowConfidenceFields) {
                Text(
                    text = stringResource(R.string.thumbnail_low_confidence_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = BudgetPilotTheme.extendedColors.warning,
                )
            }
        }
    }
}

@Composable
private fun CategorySuggestionSection(
    categories: List<Category>,
    selectedCategoryId: Long?,
    confidence: Confidence,
    isSuggested: Boolean,
    onCategorySelect: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.extraSmall),
    ) {
        Text(
            text =
                stringResource(
                    if (isSuggested) R.string.category_eyebrow_suggested else R.string.category_eyebrow,
                ),
            style = MaterialTheme.typography.labelSmall,
            color =
                if (confidence != Confidence.HIGH) {
                    BudgetPilotTheme.extendedColors.warning
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(Spacing.small)) {
            items(items = categories, key = { it.id }) { category ->
                CategoryChip(
                    label = category.name,
                    icon = categoryIcon(category.iconKey),
                    color = categoryColor(category.colorKey),
                    selected = category.id == selectedCategoryId,
                    onClick = { onCategorySelect(category.id) },
                )
            }
        }
    }
}

@Composable
private fun ActionsRow(
    isValid: Boolean,
    isSaving: Boolean,
    onRetakeClick: () -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.small),
    ) {
        TextButton(onClick = onRetakeClick, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.action_retake))
        }
        Button(onClick = onSaveClick, enabled = isValid && !isSaving, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.action_save_expense))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConfirmExpenseDatePickerDialog(
    initialDate: LocalDate,
    onDateSelect: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
) {
    val datePickerState =
        rememberDatePickerState(
            initialSelectedDateMillis = initialDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
        )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val millis = datePickerState.selectedDateMillis
                    if (millis != null) {
                        onDateSelect(Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate())
                    } else {
                        onDismiss()
                    }
                },
            ) { Text(stringResource(R.string.action_ok)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } },
    ) {
        DatePicker(state = datePickerState)
    }
}
