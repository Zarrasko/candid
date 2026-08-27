package app.candid.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.core.content.ContextCompat
import app.candid.capture.CameraController
import app.candid.capture.CameraLens
import app.candid.capture.CameraPreview
import app.candid.capture.CameraXController
import app.candid.capture.CaptureState
import app.candid.capture.HardwareCaptureButton
import app.candid.domain.JournalEntry
import app.candid.storage.EntryRepository
import app.candid.storage.PhotoFileStore
import app.candid.storage.PhotoSlot
import app.candid.theme.CandidTheme
import app.candid.theme.gridUnitsAsDp
import app.candid.ui.components.BarButton
import app.candid.ui.components.LightBottomBar
import app.candid.ui.components.LightCornerButton
import app.candid.ui.components.LightText
import app.candid.ui.components.LightTextField
import app.candid.ui.components.LightTextVariant
import app.candid.ui.components.LightTopBar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate

// Buffer after switching lenses before auto-firing the front shot, so CameraX has time to
// close the rear session and open the front one — otherwise capture can fire on a stale frame.
private const val FRONT_LENS_WARMUP_MILLIS = 600L

@Composable
fun CaptureScreen(
    photoFileStore: PhotoFileStore,
    entryRepository: EntryRepository,
    onDone: () -> Unit,
    onCancel: () -> Unit,
) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED,
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasCameraPermission = granted
    }
    LaunchedEffect(Unit) {
        if (!hasCameraPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    if (!hasCameraPermission) {
        PermissionDeniedContent(onCancel)
        return
    }

    val cameraController: CameraController = remember { CameraXController(context) }
    var state by remember { mutableStateOf<CaptureState>(CaptureState.RearPreview) }
    var isCapturing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    DisposableEffect(Unit) {
        onDispose { cameraController.unbind() }
    }

    suspend fun performCapture(isRear: Boolean) {
        if (isCapturing) return
        isCapturing = true
        val today = LocalDate.now()
        val slot = if (isRear) PhotoSlot.REAR else PhotoSlot.FRONT
        val file = photoFileStore.fileFor(today, slot)
        cameraController.capture(file)
            .onSuccess {
                state = if (isRear) {
                    CaptureState.FrontPreview
                } else {
                    CaptureState.Confirm(
                        rearFile = photoFileStore.fileFor(today, PhotoSlot.REAR),
                        frontFile = photoFileStore.fileFor(today, PhotoSlot.FRONT),
                    )
                }
            }
            .onFailure { error ->
                state = CaptureState.Error(error.message ?: "Capture failed")
            }
        isCapturing = false
    }

    // Rear capture is user-triggered (the deliberate "I'm ready" moment); the front shot
    // then fires automatically once the lens has had time to switch, so one tap covers both.
    LaunchedEffect(state) {
        when (state) {
            CaptureState.RearPreview -> cameraController.setLens(CameraLens.REAR)
            CaptureState.FrontPreview -> {
                cameraController.setLens(CameraLens.FRONT)
                delay(FRONT_LENS_WARMUP_MILLIS)
                performCapture(isRear = false)
            }
            else -> Unit
        }
    }

    when (val current = state) {
        CaptureState.RearPreview, CaptureState.FrontPreview -> {
            val isRear = current == CaptureState.RearPreview
            PreviewContent(
                cameraController = cameraController,
                onCapture = { scope.launch { performCapture(isRear) } },
                onCancel = onCancel,
            )
        }

        is CaptureState.Confirm -> {
            ConfirmContent(
                confirm = current,
                onCaptionChange = { state = current.copy(caption = it) },
                onRetake = {
                    photoFileStore.delete(LocalDate.now(), PhotoSlot.REAR)
                    photoFileStore.delete(LocalDate.now(), PhotoSlot.FRONT)
                    state = CaptureState.RearPreview
                },
                onSave = {
                    scope.launch {
                        state = CaptureState.Saving
                        entryRepository.save(
                            JournalEntry(
                                date = LocalDate.now(),
                                rearPhotoPath = current.rearFile.absolutePath,
                                frontPhotoPath = current.frontFile.absolutePath,
                                caption = current.caption,
                                capturedAtEpochMillis = System.currentTimeMillis(),
                            ),
                        )
                        onDone()
                    }
                },
            )
        }

        CaptureState.Saving -> SavingContent()

        is CaptureState.Error -> ErrorContent(
            message = current.message,
            onRetry = { state = CaptureState.RearPreview },
            onCancel = onCancel,
        )
    }
}

@Composable
private fun PreviewContent(
    cameraController: CameraController,
    onCapture: () -> Unit,
    onCancel: () -> Unit,
) {
    DisposableEffect(onCapture) {
        HardwareCaptureButton.onPressed = onCapture
        onDispose { HardwareCaptureButton.onPressed = null }
    }

    // CameraPreview fills the whole screen as a base layer, with the bars overlaid on
    // top — the embedded native camera view doesn't reliably respect a Column weight
    // slot's measured bounds, so it can't share a weighted Column with the bars.
    Box(Modifier.fillMaxSize()) {
        CameraPreview(cameraController, modifier = Modifier.fillMaxSize())
        LightCornerButton(
            label = "Cancel",
            onClick = onCancel,
            modifier = Modifier.align(Alignment.TopStart),
        )
        LightBottomBar(
            modifier = Modifier.align(Alignment.BottomStart),
            items = listOf(BarButton(label = "Capture", onClick = onCapture)),
        )
    }
}

@Composable
private fun ConfirmContent(
    confirm: CaptureState.Confirm,
    onCaptionChange: (String) -> Unit,
    onRetake: () -> Unit,
    onSave: () -> Unit,
) {
    Column(Modifier.fillMaxSize()) {
        LightTopBar(title = "Today's photo")
        Column(
            modifier = Modifier.weight(1f).fillMaxWidth().padding(gridUnitsAsDp(1f)),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(gridUnitsAsDp(0.5f)),
            ) {
                PhotoThumbnail(confirm.rearFile, Modifier.weight(1f))
                PhotoThumbnail(confirm.frontFile, Modifier.weight(1f))
            }
            LightText(
                "Caption",
                variant = LightTextVariant.Superfine,
                secondary = true,
                modifier = Modifier.padding(top = gridUnitsAsDp(1f), bottom = gridUnitsAsDp(0.25f)),
            )
            LightTextField(
                value = confirm.caption,
                onValueChange = onCaptionChange,
                placeholder = "Add a note about today...",
            )
        }
        LightBottomBar(
            items = listOf(
                BarButton(label = "Retake", onClick = onRetake),
                BarButton(label = "Save", onClick = onSave),
            ),
        )
    }
}

@Composable
private fun SavingContent() {
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        LightText("Saving...", variant = LightTextVariant.Heading)
    }
}

@Composable
private fun ErrorContent(message: String, onRetry: () -> Unit, onCancel: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.weight(1f).fillMaxWidth().padding(gridUnitsAsDp(1f)),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            LightText("Something went wrong", variant = LightTextVariant.Heading, align = TextAlign.Center)
            LightText(message, variant = LightTextVariant.Paragraph, secondary = true, align = TextAlign.Center)
        }
        LightBottomBar(
            items = listOf(
                BarButton(label = "Cancel", onClick = onCancel),
                BarButton(label = "Try again", onClick = onRetry),
            ),
        )
    }
}

@Composable
private fun PermissionDeniedContent(onCancel: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.weight(1f).fillMaxWidth().padding(gridUnitsAsDp(1f)),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            LightText("Camera access needed", variant = LightTextVariant.Heading, align = TextAlign.Center)
            LightText(
                "Candid needs camera access to capture today's photo. You can grant it from system settings.",
                variant = LightTextVariant.Paragraph,
                secondary = true,
                align = TextAlign.Center,
            )
        }
        LightBottomBar(items = listOf(BarButton(label = "Back", onClick = onCancel)))
    }
}
