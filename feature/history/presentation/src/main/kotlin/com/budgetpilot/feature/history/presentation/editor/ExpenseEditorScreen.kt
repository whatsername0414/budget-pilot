package com.budgetpilot.feature.history.presentation.editor

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.budgetpilot.core.designsystem.components.CategoryChip
import com.budgetpilot.core.designsystem.components.LoadingSkeleton
import com.budgetpilot.core.designsystem.icons.categoryIcon
import com.budgetpilot.core.designsystem.theme.Spacing
import com.budgetpilot.core.designsystem.theme.categoryColor
import com.budgetpilot.core.domain.model.Category
import com.budgetpilot.core.presentation.ObserveAsEvents
import com.budgetpilot.core.presentation.money.MoneyInputVisualTransformation
import com.budgetpilot.feature.history.presentation.R
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

private val AmountHeroFontSize = 44.sp
private val AmountHeroLineHeight = 52.sp
private val AmountHeroSkeletonHeight = 76.dp
private val CategoryChipRowHeight = 36.dp
private val TextFieldHeight = 56.dp

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
        ) {
            if (state.isLoading) {
                ExpenseEditorLoadingSkeleton(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Spacing.medium),
                )
            } else {
                ExpenseEditorForm(
                    state = state,
                    onAction = onAction,
                    onDateFieldClick = { isDatePickerVisible = true },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Spacing.medium),
                )
            }
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
private fun ExpenseEditorLoadingSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Spacing.medium),
    ) {
        LoadingSkeleton(modifier = Modifier.fillMaxWidth().height(AmountHeroSkeletonHeight))
        LoadingSkeleton(modifier = Modifier.fillMaxWidth().height(CategoryChipRowHeight))
        LoadingSkeleton(modifier = Modifier.fillMaxWidth().height(TextFieldHeight))
        LoadingSkeleton(modifier = Modifier.fillMaxWidth().height(TextFieldHeight))
        LoadingSkeleton(modifier = Modifier.fillMaxWidth().height(TextFieldHeight))
    }
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
        AmountHero(
            amountText = state.amountText,
            error = state.amountError?.asString(),
            onAmountTextChange = { onAction(ExpenseEditorAction.OnAmountTextChange(it)) },
        )

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
            label = { Text(stringResource(R.string.label_merchant)) },
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
            label = { Text(stringResource(R.string.label_note_optional)) },
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
            title = { Text(stringResource(R.string.discard_dialog_title)) },
            text = { Text(stringResource(R.string.discard_dialog_text)) },
            confirmButton = {
                TextButton(onClick = { onAction(ExpenseEditorAction.OnConfirmDiscardClick) }) {
                    Text(stringResource(R.string.action_discard))
                }
            },
            dismissButton = {
                TextButton(onClick = { onAction(ExpenseEditorAction.OnDismissDiscardDialog) }) {
                    Text(stringResource(R.string.action_keep_editing))
                }
            },
        )
    }

    if (state.isDeleteConfirmVisible) {
        AlertDialog(
            onDismissRequest = { onAction(ExpenseEditorAction.OnDismissDeleteDialog) },
            title = { Text(stringResource(R.string.delete_dialog_title)) },
            text = { Text(stringResource(R.string.delete_dialog_text)) },
            confirmButton = {
                TextButton(onClick = { onAction(ExpenseEditorAction.OnConfirmDeleteClick) }) {
                    Text(stringResource(R.string.action_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { onAction(ExpenseEditorAction.OnDismissDeleteDialog) }) {
                    Text(stringResource(R.string.action_cancel))
                }
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
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
        title = {
            Text(
                if (mode == ExpenseEditorMode.ADD) {
                    stringResource(R.string.expense_editor_title_add)
                } else {
                    stringResource(R.string.expense_editor_title_edit)
                },
            )
        },
        navigationIcon = {
            IconButton(onClick = onDismiss) {
                Icon(imageVector = Icons.Filled.Close, contentDescription = stringResource(R.string.cd_dismiss))
            }
        },
        actions = {
            if (mode == ExpenseEditorMode.EDIT) {
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = stringResource(R.string.cd_delete_expense),
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
            }
            TextButton(onClick = onSave, enabled = isValid && !isSaving) {
                Text(stringResource(R.string.action_save))
            }
        },
    )
}

@Composable
private fun AmountHero(
    amountText: String,
    error: String?,
    onAmountTextChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.label_amount),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        val amountColor = if (error != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
        val amountTextStyle =
            MaterialTheme.typography.displaySmall.copy(
                fontSize = AmountHeroFontSize,
                lineHeight = AmountHeroLineHeight,
                fontFeatureSettings = "tnum",
                color = amountColor,
            )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = stringResource(R.string.amount_currency_symbol), style = amountTextStyle)
            BasicTextField(
                modifier = Modifier.width(IntrinsicSize.Min),
                value = amountText,
                onValueChange = onAmountTextChange,
                textStyle = amountTextStyle,
                singleLine = true,
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                visualTransformation = MoneyInputVisualTransformation,
                decorationBox = { innerTextField ->
                    Box {
                        if (amountText.isEmpty()) {
                            Text(
                                text = stringResource(R.string.amount_placeholder),
                                style = amountTextStyle,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        innerTextField()
                    }
                },
            )
        }
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
        label = { Text(stringResource(R.string.label_date)) },
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
            ) { Text(stringResource(R.string.action_ok)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } },
    ) {
        DatePicker(state = datePickerState)
    }
}
