package com.budgetpilot.feature.capture.presentation.capture

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageCapture
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.budgetpilot.core.designsystem.theme.Spacing
import com.budgetpilot.core.presentation.ObserveAsEvents
import com.budgetpilot.feature.capture.presentation.R
import com.budgetpilot.feature.capture.presentation.capture.components.CameraViewfinder
import com.budgetpilot.feature.capture.presentation.capture.components.CaptureIcons
import com.budgetpilot.feature.capture.presentation.capture.components.capturePhoto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.compose.koinViewModel

private val CaptureBackground = Color(color = 0xFF0B0F1A)
private val CaptureControl = Color(color = 0xFFF1F5F9)
private const val SHUTTER_PRESSED_SCALE = 0.92f

@Composable
fun CaptureScreen(
    onNavigateBack: () -> Unit,
    onNavigateToConfirm: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CaptureViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val imageCapture = remember { ImageCapture.Builder().build() }
    val currentPermissionStatus by rememberUpdatedState(state.permissionStatus)

    val permissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            viewModel.onAction(CaptureAction.OnPermissionRequestResult(isGranted))
        }
    val galleryLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                scope.launch {
                    val bytes =
                        withContext(Dispatchers.IO) {
                            context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                        }
                    if (bytes != null) {
                        viewModel.onAction(CaptureAction.OnGalleryImageSelected(bytes))
                    } else {
                        viewModel.onAction(CaptureAction.OnCaptureError)
                    }
                }
            }
        }

    LaunchedEffect(Unit) {
        viewModel.onAction(CaptureAction.OnInitialPermissionCheck(isCameraPermissionGranted(context)))
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer =
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME && currentPermissionStatus == CameraPermissionStatus.DENIED) {
                    if (isCameraPermissionGranted(context)) {
                        viewModel.onAction(CaptureAction.OnPermissionRequestResult(true))
                    }
                }
            }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            CaptureEvent.NavigateBack -> onNavigateBack()
            CaptureEvent.RequestCameraPermission -> permissionLauncher.launch(Manifest.permission.CAMERA)
            CaptureEvent.OpenAppSettings -> context.startActivity(appSettingsIntent(context.packageName))
            is CaptureEvent.NavigateToConfirm -> onNavigateToConfirm(event.imagePath)
            is CaptureEvent.ShowError -> scope.launch { snackbarHostState.showSnackbar(event.message.asString(context)) }
        }
    }

    CaptureContent(
        state = state,
        onAction = viewModel::onAction,
        modifier = modifier,
        snackbarHostState = snackbarHostState,
        viewfinder = {
            CameraViewfinder(
                imageCapture = imageCapture,
                isTorchOn = state.isTorchOn,
                modifier = Modifier.fillMaxSize(),
            )
        },
        onShutterClick = {
            capturePhoto(
                imageCapture = imageCapture,
                executor = ContextCompat.getMainExecutor(context),
                onSuccess = { bytes -> viewModel.onAction(CaptureAction.OnPhotoCaptured(bytes)) },
                onError = { viewModel.onAction(CaptureAction.OnCaptureError) },
            )
        },
        onGalleryClick = {
            galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        },
    )
}

@Composable
fun CaptureContent(
    state: CaptureState,
    onAction: (CaptureAction) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    viewfinder: @Composable () -> Unit = {},
    onShutterClick: () -> Unit = {},
    onGalleryClick: () -> Unit = {},
) {
    val flashAlpha = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val onCapture: () -> Unit = {
        scope.launch {
            flashAlpha.snapTo(targetValue = 0.85f)
            flashAlpha.animateTo(0f, animationSpec = tween(durationMillis = 200))
        }
        onShutterClick()
    }

    Scaffold(
        modifier = modifier,
        containerColor = CaptureBackground,
        contentColor = CaptureControl,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
        ) {
            CaptureTopBar(
                isTorchOn = state.isTorchOn,
                onCloseClick = { onAction(CaptureAction.OnCloseClick) },
                onTorchToggleClick = { onAction(CaptureAction.OnTorchToggleClick) },
            )
            Box(
                modifier =
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(Spacing.medium)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.Black),
            ) {
                when (state.permissionStatus) {
                    CameraPermissionStatus.GRANTED ->
                        CaptureViewfinderArea(viewfinder = viewfinder, flashAlpha = flashAlpha.value)
                    CameraPermissionStatus.DENIED ->
                        PermissionDeniedContent(
                            onOpenSettingsClick = { onAction(CaptureAction.OnOpenSettingsClick) },
                            onGalleryClick = onGalleryClick,
                            modifier = Modifier.align(Alignment.Center),
                        )
                    CameraPermissionStatus.CHECKING -> Unit
                }
            }
            if (state.permissionStatus == CameraPermissionStatus.GRANTED) {
                CaptureBottomControls(
                    isEnabled = !state.isStoringImage,
                    onShutterClick = onCapture,
                    onGalleryClick = onGalleryClick,
                )
            }
        }
    }
}

@Composable
private fun CaptureViewfinderArea(
    viewfinder: @Composable () -> Unit,
    flashAlpha: Float,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        viewfinder()
        CornerFrameOverlay(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(Spacing.large),
        )
        Text(
            text = stringResource(R.string.capture_hint_align_receipt),
            color = CaptureControl,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = Spacing.medium),
        )
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(Color.White.copy(alpha = flashAlpha)),
        )
    }
}

@Composable
private fun CaptureTopBar(
    isTorchOn: Boolean,
    onCloseClick: () -> Unit,
    onTorchToggleClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(Spacing.small),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onCloseClick) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = stringResource(R.string.cd_close),
                tint = CaptureControl,
            )
        }
        Text(
            text = stringResource(R.string.capture_top_bar_title),
            color = CaptureControl,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f),
        )
        IconButton(onClick = onTorchToggleClick) {
            Icon(
                imageVector = if (isTorchOn) CaptureIcons.FlashOn else CaptureIcons.FlashOff,
                contentDescription = stringResource(R.string.cd_toggle_flash),
                tint = CaptureControl,
            )
        }
    }
}

@Composable
private fun CornerFrameOverlay(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val bracket = 28.dp.toPx()
        val stroke = 3.dp.toPx()
        val w = size.width
        val h = size.height
        val corners =
            listOf(
                Triple(Offset(0f, 0f), Offset(bracket, 0f), Offset(0f, bracket)),
                Triple(Offset(w, 0f), Offset(w - bracket, 0f), Offset(w, bracket)),
                Triple(Offset(0f, h), Offset(bracket, h), Offset(0f, h - bracket)),
                Triple(Offset(w, h), Offset(w - bracket, h), Offset(w, h - bracket)),
            )
        corners.forEach { (corner, armX, armY) ->
            drawLine(color = CaptureControl, start = corner, end = armX, strokeWidth = stroke)
            drawLine(color = CaptureControl, start = corner, end = armY, strokeWidth = stroke)
        }
    }
}

@Composable
private fun PermissionDeniedContent(
    onOpenSettingsClick: () -> Unit,
    onGalleryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(Spacing.large),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = CaptureIcons.PhotoCamera,
            contentDescription = null,
            tint = CaptureControl,
            modifier =
                Modifier
                    .size(48.dp)
                    .padding(bottom = Spacing.medium),
        )
        Text(
            text = stringResource(R.string.permission_denied_title),
            color = CaptureControl,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(R.string.permission_denied_description),
            color = CaptureControl.copy(alpha = 0.8f),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = Spacing.small, bottom = Spacing.large),
        )
        Button(onClick = onOpenSettingsClick) {
            Text(stringResource(R.string.action_open_settings))
        }
        OutlinedButton(
            onClick = onGalleryClick,
            modifier = Modifier.padding(top = Spacing.small),
        ) {
            Text(stringResource(R.string.action_choose_from_gallery))
        }
    }
}

@Composable
private fun CaptureBottomControls(
    isEnabled: Boolean,
    onShutterClick: () -> Unit,
    onGalleryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(Spacing.large),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.weight(1f)) {
            GalleryTile(
                isEnabled = isEnabled,
                onClick = onGalleryClick,
                modifier = Modifier.align(Alignment.CenterStart),
            )
        }
        ShutterButton(isEnabled = isEnabled, onClick = onShutterClick)
        Box(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun GalleryTile(
    isEnabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.12f))
                .clickable(enabled = isEnabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = CaptureIcons.PhotoLibrary,
            contentDescription = stringResource(R.string.cd_choose_from_gallery),
            tint = CaptureControl,
        )
    }
}

@Composable
private fun ShutterButton(
    isEnabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val haptic = LocalHapticFeedback.current

    Box(
        modifier =
            modifier
                .size(76.dp)
                .graphicsLayer {
                    val scale = if (isPressed) SHUTTER_PRESSED_SCALE else 1f
                    scaleX = scale
                    scaleY = scale
                }.clip(CircleShape)
                .border(width = 3.dp, color = CaptureControl, shape = CircleShape)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    enabled = isEnabled,
                ) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onClick()
                }.padding(6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(CaptureControl),
        )
    }
}
