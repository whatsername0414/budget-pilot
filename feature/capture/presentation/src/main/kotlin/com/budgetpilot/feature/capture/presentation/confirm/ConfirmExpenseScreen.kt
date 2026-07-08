package com.budgetpilot.feature.capture.presentation.confirm

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.budgetpilot.core.designsystem.components.AppCard
import com.budgetpilot.core.designsystem.components.AppTopBar
import com.budgetpilot.core.designsystem.components.ErrorState
import com.budgetpilot.core.designsystem.components.LoadingSkeleton
import com.budgetpilot.core.designsystem.icons.StateIcons
import com.budgetpilot.core.designsystem.theme.Spacing
import com.budgetpilot.core.presentation.ObserveAsEvents
import com.budgetpilot.feature.capture.domain.model.ReceiptType
import com.budgetpilot.feature.capture.presentation.R
import com.budgetpilot.feature.capture.presentation.confirm.components.LineItemEditorSheet
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.io.File
import com.budgetpilot.core.designsystem.R as DesignSystemR

@Composable
fun ConfirmExpenseScreen(
    onNavigateBack: () -> Unit,
    onNavigateToRetake: () -> Unit,
    onSaveSuccess: (confirmationMessage: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ConfirmExpenseViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            ConfirmExpenseEvent.NavigateBack -> onNavigateBack()
            ConfirmExpenseEvent.NavigateToRetake -> onNavigateToRetake()
            is ConfirmExpenseEvent.NavigateHome -> onSaveSuccess(event.confirmationMessage)
            is ConfirmExpenseEvent.ShowError -> {
                scope.launch { snackbarHostState.showSnackbar(event.message.asString(context)) }
            }
        }
    }

    ConfirmExpenseContent(
        state = state,
        onAction = viewModel::onAction,
        modifier = modifier,
        snackbarHostState = snackbarHostState,
    )
}

@Composable
fun ConfirmExpenseContent(
    state: ConfirmExpenseState,
    onAction: (ConfirmExpenseAction) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            if (state.phase == ConfirmExpensePhase.LOADING) {
                ConfirmExpenseLoadingTopBar(
                    onCancelClick = { onAction(ConfirmExpenseAction.OnRetakeClick) },
                )
            } else {
                ConfirmExpenseTopBar(
                    receiptType = state.receiptType,
                    onBackClick = { onAction(ConfirmExpenseAction.OnBackClick) },
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
        ) {
            when (state.phase) {
                ConfirmExpensePhase.LOADING ->
                    ConfirmExpenseLoadingSection(
                        imagePath = state.imagePath,
                        stagedStatusText = state.stagedStatusText.asString(),
                        onCancelClick = { onAction(ConfirmExpenseAction.OnRetakeClick) },
                    )
                ConfirmExpensePhase.ERROR ->
                    ErrorState(
                        message = state.errorMessage?.asString().orEmpty(),
                        onRetry = {
                            val action =
                                if (state.canUseOfflineScan) {
                                    ConfirmExpenseAction.OnRetryOnDeviceClick
                                } else {
                                    ConfirmExpenseAction.OnRetryExtractionClick
                                }
                            onAction(action)
                        },
                        retryLabel =
                            if (state.canUseOfflineScan) {
                                stringResource(R.string.action_use_offline_scan)
                            } else {
                                stringResource(DesignSystemR.string.action_retry)
                            },
                        secondaryActionLabel = stringResource(R.string.action_enter_manually),
                        onSecondaryAction = { onAction(ConfirmExpenseAction.OnEnterManuallyClick) },
                        modifier = Modifier.align(Alignment.Center),
                    )
                ConfirmExpensePhase.LOADED ->
                    ConfirmExpenseLoadedContent(state = state, onAction = onAction)
            }
        }
    }

    if (state.isImageViewerVisible) {
        ImageViewerDialog(
            imagePath = state.imagePath,
            onDismiss = { onAction(ConfirmExpenseAction.OnDismissImageViewer) },
        )
    }

    if (state.isLineItemSheetVisible) {
        LineItemEditorSheet(state = state, onAction = onAction)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConfirmExpenseTopBar(
    receiptType: ReceiptType,
    onBackClick: () -> Unit,
) {
    AppTopBar(
        title = stringResource(R.string.confirm_expense_title),
        onNavigateBack = onBackClick,
        navigateBackContentDescription = stringResource(R.string.cd_back),
        actions = { SourceBadge(receiptType = receiptType, modifier = Modifier.padding(end = Spacing.medium)) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConfirmExpenseLoadingTopBar(onCancelClick: () -> Unit) {
    TopAppBar(
        title = { Text(text = stringResource(R.string.reading_receipt_title), style = MaterialTheme.typography.titleLarge) },
        navigationIcon = {
            IconButton(onClick = onCancelClick) {
                Icon(imageVector = Icons.Filled.Close, contentDescription = stringResource(R.string.cd_close))
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
    )
}

@Composable
private fun SourceBadge(
    receiptType: ReceiptType,
    modifier: Modifier = Modifier,
) {
    val label =
        when (receiptType) {
            ReceiptType.PAPER -> stringResource(R.string.source_badge_receipt)
            ReceiptType.GCASH -> stringResource(R.string.source_badge_gcash)
            ReceiptType.MAYA -> stringResource(R.string.source_badge_maya)
        }
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(4.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(horizontal = Spacing.small, vertical = 3.dp),
        )
    }
}

@Composable
private fun ConfirmExpenseLoadingSection(
    imagePath: String,
    stagedStatusText: String,
    onCancelClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(Spacing.medium),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        AsyncImage(
            model = File(imagePath),
            contentDescription = null,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(132.dp)
                    .clip(RoundedCornerShape(16.dp)),
            contentScale = ContentScale.Crop,
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(Spacing.extraSmall),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = StateIcons.Sparkle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
            )
            Text(
                text = stagedStatusText,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        AppCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
                LoadingSkeleton(
                    shape = RoundedCornerShape(6.dp),
                    modifier =
                        Modifier
                            .fillMaxWidth(LOADING_TITLE_SKELETON_WIDTH_FRACTION)
                            .height(20.dp),
                )
                LoadingSkeleton(modifier = Modifier.fillMaxWidth().height(44.dp))
                LoadingSkeleton(modifier = Modifier.fillMaxWidth().height(44.dp))
                LoadingSkeleton(
                    modifier =
                        Modifier
                            .fillMaxWidth(LOADING_LAST_FIELD_SKELETON_WIDTH_FRACTION)
                            .height(44.dp),
                )
            }
        }
        TextButton(onClick = onCancelClick) {
            Text(stringResource(R.string.action_cancel))
        }
    }
}

private const val LOADING_TITLE_SKELETON_WIDTH_FRACTION = 0.45f
private const val LOADING_LAST_FIELD_SKELETON_WIDTH_FRACTION = 0.70f

@Composable
private fun ImageViewerDialog(
    imagePath: String,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(8.dp)),
        ) {
            AsyncImage(
                model = File(imagePath),
                contentDescription = stringResource(R.string.cd_receipt_photo),
                modifier =
                    Modifier
                        .fillMaxSize()
                        .align(Alignment.Center),
                contentScale = ContentScale.Fit,
            )
        }
    }
}
