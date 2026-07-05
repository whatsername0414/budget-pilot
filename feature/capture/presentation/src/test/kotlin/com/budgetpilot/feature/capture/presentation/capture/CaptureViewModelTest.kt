package com.budgetpilot.feature.capture.presentation.capture

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isTrue
import com.budgetpilot.feature.capture.presentation.fake.FakeReceiptImageStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CaptureViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel(receiptImageStore: FakeReceiptImageStore = FakeReceiptImageStore()) = CaptureViewModel(receiptImageStore)

    @Test
    fun `initial permission check granted updates state to granted`() =
        runTest {
            val viewModel = viewModel()

            viewModel.state.test {
                assertThat(awaitItem().permissionStatus).isEqualTo(CameraPermissionStatus.CHECKING)
                viewModel.onAction(CaptureAction.OnInitialPermissionCheck(isGranted = true))
                assertThat(awaitItem().permissionStatus).isEqualTo(CameraPermissionStatus.GRANTED)
            }
        }

    @Test
    fun `initial permission check denied requests system permission prompt`() =
        runTest {
            val viewModel = viewModel()

            viewModel.events.test {
                viewModel.onAction(CaptureAction.OnInitialPermissionCheck(isGranted = false))
                assertThat(awaitItem()).isEqualTo(CaptureEvent.RequestCameraPermission)
            }
            assertThat(viewModel.state.value.permissionStatus).isEqualTo(CameraPermissionStatus.CHECKING)
        }

    @Test
    fun `permission request result granted updates state to granted`() =
        runTest {
            val viewModel = viewModel()

            viewModel.onAction(CaptureAction.OnPermissionRequestResult(isGranted = true))

            assertThat(viewModel.state.value.permissionStatus).isEqualTo(CameraPermissionStatus.GRANTED)
        }

    @Test
    fun `permission request result denied updates state to denied`() =
        runTest {
            val viewModel = viewModel()

            viewModel.onAction(CaptureAction.OnPermissionRequestResult(isGranted = false))

            assertThat(viewModel.state.value.permissionStatus).isEqualTo(CameraPermissionStatus.DENIED)
        }

    @Test
    fun `resuming after granting in settings flips denied back to granted`() =
        runTest {
            val viewModel = viewModel()
            viewModel.onAction(CaptureAction.OnPermissionRequestResult(isGranted = false))

            viewModel.onAction(CaptureAction.OnPermissionRequestResult(isGranted = true))

            assertThat(viewModel.state.value.permissionStatus).isEqualTo(CameraPermissionStatus.GRANTED)
        }

    @Test
    fun `torch toggle click flips isTorchOn`() =
        runTest {
            val viewModel = viewModel()

            viewModel.onAction(CaptureAction.OnTorchToggleClick)
            assertThat(viewModel.state.value.isTorchOn).isTrue()

            viewModel.onAction(CaptureAction.OnTorchToggleClick)
            assertThat(viewModel.state.value.isTorchOn).isFalse()
        }

    @Test
    fun `close click emits navigate back event`() =
        runTest {
            val viewModel = viewModel()

            viewModel.events.test {
                viewModel.onAction(CaptureAction.OnCloseClick)
                assertThat(awaitItem()).isEqualTo(CaptureEvent.NavigateBack)
            }
        }

    @Test
    fun `photo captured stores image and navigates to confirm with the stored path`() =
        runTest {
            val receiptImageStore = FakeReceiptImageStore()
            val viewModel = viewModel(receiptImageStore)
            val bytes = byteArrayOf(1, 2, 3)

            viewModel.events.test {
                viewModel.onAction(CaptureAction.OnPhotoCaptured(bytes))
                assertThat(awaitItem()).isEqualTo(CaptureEvent.NavigateToConfirm("receipts/0.jpg"))
            }
            assertThat(receiptImageStore.savedBytes.single()).isEqualTo(bytes)
            assertThat(viewModel.state.value.isStoringImage).isFalse()
        }

    @Test
    fun `gallery image selected stores image and navigates to confirm`() =
        runTest {
            val receiptImageStore = FakeReceiptImageStore()
            val viewModel = viewModel(receiptImageStore)
            val bytes = byteArrayOf(4, 5, 6)

            viewModel.events.test {
                viewModel.onAction(CaptureAction.OnGalleryImageSelected(bytes))
                assertThat(awaitItem()).isEqualTo(CaptureEvent.NavigateToConfirm("receipts/0.jpg"))
            }
        }

    @Test
    fun `image store failure shows an error instead of navigating`() =
        runTest {
            val receiptImageStore = FakeReceiptImageStore().apply { shouldReturnError = true }
            val viewModel = viewModel(receiptImageStore)

            viewModel.events.test {
                viewModel.onAction(CaptureAction.OnPhotoCaptured(byteArrayOf(1)))
                assertThat(awaitItem()).isInstanceOf(CaptureEvent.ShowError::class)
            }
            assertThat(viewModel.state.value.isStoringImage).isFalse()
        }

    @Test
    fun `capture error action shows an error`() =
        runTest {
            val viewModel = viewModel()

            viewModel.events.test {
                viewModel.onAction(CaptureAction.OnCaptureError)
                assertThat(awaitItem()).isInstanceOf(CaptureEvent.ShowError::class)
            }
        }
}
