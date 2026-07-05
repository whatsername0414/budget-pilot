package com.budgetpilot.feature.capture.presentation.capture

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgetpilot.core.domain.DataError
import com.budgetpilot.core.domain.onFailure
import com.budgetpilot.core.domain.onSuccess
import com.budgetpilot.core.presentation.toUiText
import com.budgetpilot.feature.capture.domain.ReceiptImageStore
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CaptureViewModel(
    private val receiptImageStore: ReceiptImageStore,
) : ViewModel() {
    private val _state = MutableStateFlow(CaptureState())
    val state = _state.asStateFlow()

    private val _events = Channel<CaptureEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun onAction(action: CaptureAction) {
        when (action) {
            CaptureAction.OnCloseClick -> sendEvent(CaptureEvent.NavigateBack)
            CaptureAction.OnTorchToggleClick -> _state.update { it.copy(isTorchOn = !it.isTorchOn) }
            is CaptureAction.OnInitialPermissionCheck -> onInitialPermissionCheck(action.isGranted)
            is CaptureAction.OnPermissionRequestResult -> onPermissionRequestResult(action.isGranted)
            CaptureAction.OnOpenSettingsClick -> sendEvent(CaptureEvent.OpenAppSettings)
            is CaptureAction.OnPhotoCaptured -> storeImage(action.bytes)
            is CaptureAction.OnGalleryImageSelected -> storeImage(action.bytes)
            CaptureAction.OnCaptureError -> sendEvent(CaptureEvent.ShowError(DataError.Local.UNKNOWN.toUiText()))
        }
    }

    private fun onInitialPermissionCheck(isGranted: Boolean) {
        if (isGranted) {
            _state.update { it.copy(permissionStatus = CameraPermissionStatus.GRANTED) }
        } else {
            sendEvent(CaptureEvent.RequestCameraPermission)
        }
    }

    private fun onPermissionRequestResult(isGranted: Boolean) {
        _state.update {
            it.copy(
                permissionStatus = if (isGranted) CameraPermissionStatus.GRANTED else CameraPermissionStatus.DENIED,
            )
        }
    }

    private fun storeImage(bytes: ByteArray) {
        if (_state.value.isStoringImage) return
        _state.update { it.copy(isStoringImage = true) }
        viewModelScope.launch {
            receiptImageStore
                .save(bytes)
                .onSuccess { path ->
                    _state.update { it.copy(isStoringImage = false) }
                    _events.send(CaptureEvent.NavigateToConfirm(path))
                }.onFailure { error ->
                    _state.update { it.copy(isStoringImage = false) }
                    _events.send(CaptureEvent.ShowError(error.toUiText()))
                }
        }
    }

    private fun sendEvent(event: CaptureEvent) {
        viewModelScope.launch { _events.send(event) }
    }
}
