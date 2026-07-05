package com.budgetpilot.feature.capture.presentation.capture.components

import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.io.ByteArrayOutputStream
import java.util.concurrent.Executor

@Composable
internal fun CameraViewfinder(
    imageCapture: ImageCapture,
    isTorchOn: Boolean,
    modifier: Modifier = Modifier,
) {
    if (LocalInspectionMode.current) return

    val lifecycleOwner = LocalLifecycleOwner.current
    var camera by remember { mutableStateOf<Camera?>(null) }

    AndroidView(
        modifier = modifier,
        factory = { viewContext ->
            val previewView =
                PreviewView(viewContext).apply {
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                }
            val cameraProviderFuture = ProcessCameraProvider.getInstance(viewContext)
            cameraProviderFuture.addListener(
                {
                    val preview =
                        Preview.Builder().build().apply {
                            surfaceProvider = previewView.surfaceProvider
                        }
                    val cameraProvider = cameraProviderFuture.get()
                    cameraProvider.unbindAll()
                    camera =
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            imageCapture,
                        )
                },
                ContextCompat.getMainExecutor(viewContext),
            )
            previewView
        },
    )

    LaunchedEffect(isTorchOn, camera) {
        camera?.cameraControl?.enableTorch(isTorchOn)
    }
}

internal fun capturePhoto(
    imageCapture: ImageCapture,
    executor: Executor,
    onSuccess: (ByteArray) -> Unit,
    onError: () -> Unit,
) {
    val outputStream = ByteArrayOutputStream()
    val outputOptions = ImageCapture.OutputFileOptions.Builder(outputStream).build()
    imageCapture.takePicture(
        outputOptions,
        executor,
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                onSuccess(outputStream.toByteArray())
            }

            override fun onError(exception: ImageCaptureException) {
                onError()
            }
        },
    )
}
