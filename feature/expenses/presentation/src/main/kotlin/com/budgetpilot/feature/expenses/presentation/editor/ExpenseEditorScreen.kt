package com.budgetpilot.feature.expenses.presentation.editor

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.budgetpilot.core.designsystem.components.AmountText
import com.budgetpilot.core.designsystem.components.CategoryChip
import com.budgetpilot.core.designsystem.icons.categoryIcon
import com.budgetpilot.core.designsystem.theme.Spacing
import com.budgetpilot.core.designsystem.theme.categoryColor
import com.budgetpilot.core.domain.model.Category
import com.budgetpilot.core.domain.money.Money
import com.budgetpilot.core.presentation.ObserveAsEvents
import com.budgetpilot.feature.expenses.presentation.editor.components.AmountKeypad
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun ExpenseEditorScreen(
    onNavigateBack: (confirmationMessage: String?) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ExpenseEditorViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is ExpenseEditorEvent.NavigateBack -> onNavigateBack(event.confirmationMessage)
            is ExpenseEditorEvent.ShowError -> {
                scope.launch { snackbarHostState.showSnackbar(message = event.message.asString(context)) }
            }
        }
    }

    ExpenseEditorContent(
        state = state,
        onAction = viewModel::onAction,
        modifier = modifier,
        snackbarHostState = snackbarHostState,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseEditorContent(
    state: ExpenseEditorState,
    onAction: (ExpenseEditorAction) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    var isDatePickerVisible by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            ExpenseEditorTopBar(
                mode = state.mode,
                isValid = state.isValid,
                isSaving = state.isSaving,
                onDismiss = { onAction(ExpenseEditorAction.OnDismissClick) },
                onSave = { onAction(ExpenseEditorAction.OnSaveClick) },
                onDelete = { onAction(ExpenseEditorAction.OnDeleteClick) },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            ExpenseEditorForm(
                state = state,
                onAction = onAction,
                onDateFieldClick = { isDatePickerVisible = true },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.medium),
            )

            AmountKeypad(
                onKeyPress = { onAction(ExpenseEditorAction.OnAmountKeyPress(it)) },
                modifier = Modifier.padding(Spacing.medium),
            )
        }
    }

    ExpenseEditorDialogs(
        state = state,
        onAction = onAction,
        isDatePickerVisible = isDatePickerVisible,
        onDismissDatePicker = { isDatePickerVisible = false },
    )
}

@Composable
private fun ExpenseEditorForm(
    state: ExpenseEditorState,
    onAction: (ExpenseEditorAction) -> Unit,
    onDateFieldClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Spacing.medium),
    ) {
        AmountHero(amount = state.displayAmount, error = state.amountError?.asString())

        CategoryChipRow(
            categories = state.categories,
            selectedCategoryId = state.selectedCategoryId,
            onCategorySelect = { onAction(ExpenseEditorAction.OnCategorySelect(it)) },
        )

        var merchantHasBeenFocused by remember { mutableStateOf(false) }
        OutlinedTextField(
            value = state.merchant,
            onValueChange = { onAction(ExpenseEditorAction.OnMerchantChange(it)) },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .onFocusChanged { focusState ->
                        if (focusState.isFocused) {
                            merchantHasBeenFocused = true
                        } else if (merchantHasBeenFocused) {
                            onAction(ExpenseEditorAction.OnMerchantFieldBlur)
                        }
                    },
            label = { Text("Merchant") },
            isError = state.merchantError != null,
            supportingText = { state.merchantError?.let { Text(it.asString()) } },
            singleLine = true,
        )

        DateField(
            date = state.date,
            error = state.dateError?.asString(),
            onClick = onDateFieldClick,
        )

        OutlinedTextField(
            value = state.note,
            onValueChange = { onAction(ExpenseEditorAction.OnNoteChange(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Note (optional)") },
            singleLine = true,
        )
    }
}

@Composable
private fun ExpenseEditorDialogs(
    state: ExpenseEditorState,
    onAction: (ExpenseEditorAction) -> Unit,
    isDatePickerVisible: Boolean,
    onDismissDatePicker: () -> Unit,
) {
    if (isDatePickerVisible) {
        ExpenseDatePickerDialog(
            initialDate = state.date,
            onDateSelect = {
                onAction(ExpenseEditorAction.OnDateSelect(it))
                onDismissDatePicker()
            },
            onDismiss = onDismissDatePicker,
        )
    }

    if (state.isDiscardConfirmVisible) {
        AlertDialog(
            onDismissRequest = { onAction(ExpenseEditorAction.OnDismissDiscardDialog) },
            title = { Text("Discard changes?") },
            text = { Text("Your edits haven't been saved yet.") },
            confirmButton = {
                TextButton(onClick = { onAction(ExpenseEditorAction.OnConfirmDiscardClick) }) { Text("Discard") }
            },
            dismissButton = {
                TextButton(onClick = { onAction(ExpenseEditorAction.OnDismissDiscardDialog) }) { Text("Keep editing") }
            },
        )
    }

    if (state.isDeleteConfirmVisible) {
        AlertDialog(
            onDismissRequest = { onAction(ExpenseEditorAction.OnDismissDeleteDialog) },
            title = { Text("Delete expense?") },
            text = { Text("This can't be undone.") },
            confirmButton = {
                TextButton(onClick = { onAction(ExpenseEditorAction.OnConfirmDeleteClick) }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { onAction(ExpenseEditorAction.OnDismissDeleteDialog) }) { Text("Cancel") }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExpenseEditorTopBar(
    mode: ExpenseEditorMode,
    isValid: Boolean,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
) {
    TopAppBar(
        title = { Text(if (mode == ExpenseEditorMode.ADD) "Add expense" else "Edit expense") },
        navigationIcon = {
            IconButton(onClick = onDismiss) {
                Icon(imageVector = Icons.Filled.Close, contentDescription = "Dismiss")
            }
        },
        actions = {
            if (mode == ExpenseEditorMode.EDIT) {
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete expense",
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
            }
            TextButton(onClick = onSave, enabled = isValid && !isSaving) {
                Text("Save")
            }
        },
    )
}

@Composable
private fun AmountHero(
    amount: Money,
    error: String?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "AMOUNT",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        AmountText(
            amount = amount,
            style = MaterialTheme.typography.displaySmall,
            color = if (error != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
        )
        if (error != null) {
            Text(
                text = error,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun CategoryChipRow(
    categories: List<Category>,
    selectedCategoryId: Long?,
    onCategorySelect: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.small),
        contentPadding = PaddingValues(vertical = Spacing.extraSmall),
    ) {
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

@Composable
private fun DateField(
    date: LocalDate,
    error: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val currentOnClick by rememberUpdatedState(onClick)
    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            if (interaction is PressInteraction.Release) currentOnClick()
        }
    }

    OutlinedTextField(
        value = date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)),
        onValueChange = {},
        modifier = modifier.fillMaxWidth(),
        label = { Text("Date") },
        isError = error != null,
        supportingText = { error?.let { Text(it) } },
        readOnly = true,
        interactionSource = interactionSource,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExpenseDatePickerDialog(
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
            ) { Text("OK") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    ) {
        DatePicker(state = datePickerState)
    }
}
