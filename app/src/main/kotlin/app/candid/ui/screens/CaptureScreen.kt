package app.candid.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import app.candid.capture.CaptureOrder
import app.candid.capture.CaptureSettings
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
import app.candid.ui.components.hairlineBorder
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate

// Every tap of Capture - first shot, or a manual second shot - runs this countdown before the
// shutter actually fires, and auto-advancing into the second shot runs the same one
// automatically. Early beta feedback was twofold: the second camera fired before its preview
// had rendered, and the capture button itself felt unresponsive because nothing visible
// happened between the tap and the shutter - a visible, predictable countdown fixes both.
private const val CAPTURE_COUNTDOWN_MILLIS = 3000L
private const val COUNTDOWN_TICK_MILLIS = 1000L

@Composable
fun CaptureScreen(
    photoFileStore: PhotoFileStore,
    entryRepository: EntryRepository,
    captureSettings: CaptureSettings,
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
    var state by remember { mutableStateOf<CaptureState>(CaptureState.FirstPreview) }
    var isCapturing by remember { mutableStateOf(false) }
    var countdownSeconds by remember { mutableStateOf<Int?>(null) }
    var countdownJob by remember { mutableStateOf<Job?>(null) }
    val autoDualCapture = remember { captureSettings.isAutoDualCaptureEnabled() }
    // CaptureOrder decides which physical camera is "first" vs "second" - the state machine
    // itself never refers to rear/front, only first/second, so this is the one place that
    // translates between them.
    val captureOrder = remember { captureSettings.getCaptureOrder() }
    val firstSlot = if (captureOrder == CaptureOrder.REAR_FIRST) PhotoSlot.REAR else PhotoSlot.FRONT
    val secondSlot = if (captureOrder == CaptureOrder.REAR_FIRST) PhotoSlot.FRONT else PhotoSlot.REAR
    val scope = rememberCoroutineScope()

    DisposableEffect(Unit) {
        onDispose { cameraController.unbind() }
    }

    suspend fun performCapture(slot: PhotoSlot, isFirst: Boolean) {
        if (isCapturing) return
        isCapturing = true
        val today = LocalDate.now()
        val file = photoFileStore.fileFor(today, slot)
        cameraController.capture(file)
            .onSuccess {
                state = if (isFirst) {
                    CaptureState.SecondPreview
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

    // The single entry point for firing a shot, whether triggered by a tap or by auto-advance.
    // A tap that lands while a countdown is already running (from auto-advance, or an earlier
    // tap) means "go now" - it cancels the wait and captures immediately rather than queuing a
    // second countdown behind it.
    fun startCapture(slot: PhotoSlot, isFirst: Boolean) {
        if (isCapturing) return
        if (countdownSeconds != null) {
            countdownJob?.cancel()
            countdownSeconds = null
            scope.launch { performCapture(slot, isFirst) }
            return
        }
        countdownJob = scope.launch {
            val totalSeconds = (CAPTURE_COUNTDOWN_MILLIS / COUNTDOWN_TICK_MILLIS).toInt()
            for (remaining in totalSeconds downTo 1) {
                countdownSeconds = remaining
                delay(COUNTDOWN_TICK_MILLIS)
            }
            countdownSeconds = null
            performCapture(slot, isFirst)
        }
    }

    LaunchedEffect(state) {
        when (state) {
            CaptureState.FirstPreview -> {
                cameraController.setLens(if (firstSlot == PhotoSlot.REAR) CameraLens.REAR else CameraLens.FRONT)
                countdownSeconds = null
            }
            CaptureState.SecondPreview -> {
                cameraController.setLens(if (secondSlot == PhotoSlot.REAR) CameraLens.REAR else CameraLens.FRONT)
                if (autoDualCapture) {
                    startCapture(secondSlot, isFirst = false)
                }
            }
            else -> Unit
        }
    }

    when (val current = state) {
        CaptureState.FirstPreview, CaptureState.SecondPreview -> {
            val isFirst = current == CaptureState.FirstPreview
            val slot = if (isFirst) firstSlot else secondSlot
            PreviewContent(
                cameraController = cameraController,
                onCapture = { startCapture(slot, isFirst) },
                countdownSeconds = countdownSeconds,
                onSkip = if (!isFirst && !autoDualCapture) {
                    {
                        countdownJob?.cancel()
                        countdownSeconds = null
                        val firstFile = photoFileStore.fileFor(LocalDate.now(), firstSlot)
                        state = CaptureState.Confirm(
                            rearFile = if (firstSlot == PhotoSlot.REAR) firstFile else null,
                            frontFile = if (firstSlot == PhotoSlot.FRONT) firstFile else null,
                        )
                    }
                } else {
                    null
                },
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
                    state = CaptureState.FirstPreview
                },
                onSave = {
                    scope.launch {
                        state = CaptureState.Saving
                        entryRepository.save(
                            JournalEntry(
                                date = LocalDate.now(),
                                rearPhotoPath = current.rearFile?.absolutePath,
                                frontPhotoPath = current.frontFile?.absolutePath,
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
            onRetry = { state = CaptureState.FirstPreview },
            onCancel = onCancel,
        )
    }
}

@Composable
private fun PreviewContent(
    cameraController: CameraController,
    onCapture: () -> Unit,
    countdownSeconds: Int?,
    onSkip: (() -> Unit)?,
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
        if (countdownSeconds != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .hairlineBorder(CandidTheme.colors.content)
                    .background(CandidTheme.colors.background.copy(alpha = 0.75f))
                    .padding(horizontal = gridUnitsAsDp(2f), vertical = gridUnitsAsDp(1f)),
                contentAlignment = Alignment.Center,
            ) {
                LightText(text = countdownSeconds.toString(), variant = LightTextVariant.Title)
            }
        }
        LightBottomBar(
            modifier = Modifier.align(Alignment.BottomStart),
            items = listOfNotNull(
                BarButton(label = "Capture", onClick = onCapture),
                onSkip?.let { BarButton(label = "Skip", onClick = it) },
            ),
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
