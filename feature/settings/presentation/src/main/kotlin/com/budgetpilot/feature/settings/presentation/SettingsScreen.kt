package com.budgetpilot.feature.settings.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.budgetpilot.core.designsystem.components.AppCard
import com.budgetpilot.core.designsystem.components.AppTopBar
import com.budgetpilot.core.designsystem.theme.BudgetPilotTheme
import com.budgetpilot.core.designsystem.theme.Spacing
import com.budgetpilot.core.presentation.ObserveAsEvents
import com.budgetpilot.feature.settings.presentation.R
import com.budgetpilot.feature.settings.presentation.components.SettingRow
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.util.Locale

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is SettingsEvent.ShowError -> {
                scope.launch { snackbarHostState.showSnackbar(message = event.message.asString(context)) }
            }
        }
    }

    SettingsContent(
        state = state,
        onAction = viewModel::onAction,
        onNavigateBack = onNavigateBack,
        modifier = modifier,
        snackbarHostState = snackbarHostState,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsContent(
    state: SettingsState,
    onAction: (SettingsAction) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            AppTopBar(
                title = stringResource(R.string.settings_top_bar_title),
                onNavigateBack = onNavigateBack,
                navigateBackContentDescription = stringResource(R.string.cd_back),
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).padding(Spacing.medium)) {
            Text(
                text = stringResource(R.string.settings_section_ai_privacy).uppercase(Locale.ENGLISH),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(Spacing.small))
            AppCard(modifier = Modifier.fillMaxWidth()) {
                SettingRow(
                    title = stringResource(R.string.settings_cloud_ai_title),
                    description = stringResource(R.string.settings_cloud_ai_description),
                    onClick = { onAction(SettingsAction.OnCloudAiToggle(!state.cloudAiEnabled)) },
                    trailingContent = {
                        Switch(
                            checked = state.cloudAiEnabled,
                            onCheckedChange = { enabled -> onAction(SettingsAction.OnCloudAiToggle(enabled)) },
                        )
                    },
                )
                HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
                SettingRow(
                    title = stringResource(R.string.settings_api_key_title),
                    description = stringResource(R.string.settings_api_key_description),
                    trailingContent = { ApiKeyStatusChip(isConfigured = state.isApiKeyConfigured) },
                )
            }
        }
    }
}

@Composable
private fun ApiKeyStatusChip(
    isConfigured: Boolean,
    modifier: Modifier = Modifier,
) {
    val containerColor =
        if (isConfigured) {
            MaterialTheme.colorScheme.tertiaryContainer
        } else {
            BudgetPilotTheme.extendedColors.warningContainer
        }
    val contentColor =
        if (isConfigured) {
            MaterialTheme.colorScheme.onTertiaryContainer
        } else {
            BudgetPilotTheme.extendedColors.onWarningContainer
        }
    Surface(
        modifier = modifier,
        color = containerColor,
        contentColor = contentColor,
        shape = RoundedCornerShape(Spacing.small),
    ) {
        Text(
            text =
                stringResource(
                    if (isConfigured) R.string.settings_api_key_status_active else R.string.settings_api_key_status_missing,
                ),
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = Spacing.small, vertical = Spacing.extraSmall),
        )
    }
}

@PreviewLightDark
@Composable
private fun SettingsScreenPreview() {
    BudgetPilotTheme {
        SettingsContent(
            state = SettingsState(cloudAiEnabled = true, isApiKeyConfigured = true, isLoading = false),
            onAction = {},
            onNavigateBack = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun SettingsScreenMissingKeyPreview() {
    BudgetPilotTheme {
        SettingsContent(
            state = SettingsState(cloudAiEnabled = false, isApiKeyConfigured = false, isLoading = false),
            onAction = {},
            onNavigateBack = {},
        )
    }
}
