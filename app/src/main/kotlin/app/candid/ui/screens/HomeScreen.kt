package app.candid.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import app.candid.domain.JournalEntry
import app.candid.storage.EntryRepository
import app.candid.theme.CandidTheme
import app.candid.theme.gridUnitsAsDp
import app.candid.ui.components.BarButton
import app.candid.ui.components.LightBottomBar
import app.candid.ui.components.LightText
import app.candid.ui.components.LightTextVariant
import app.candid.ui.components.LightTopBar
import app.candid.ui.components.hairlineBorder
import app.candid.ui.rememberSampledBitmap
import kotlinx.coroutines.flow.map
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun HomeScreen(
    entryRepository: EntryRepository,
    onCapture: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val todayEntry by remember(entryRepository) {
        entryRepository.observeAll().map { entries -> entries.firstOrNull { it.date == LocalDate.now() } }
    }.collectAsState(initial = null)

    val context = LocalContext.current
    val notificationPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}
    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    Column(Modifier.fillMaxSize()) {
        LightTopBar(title = "Candid")

        Column(
            modifier = Modifier.weight(1f).fillMaxWidth().padding(gridUnitsAsDp(1f)),
            verticalArrangement = Arrangement.Center,
        ) {
            if (todayEntry == null) {
                EmptyTodayState()
            } else {
                TodayEntryPreview(todayEntry!!, onRetake = onCapture)
            }
        }

        LightBottomBar(
            items = listOf(
                BarButton(label = if (todayEntry == null) "Capture" else "Retake", onClick = onCapture),
                BarButton(label = "History", onClick = onOpenHistory),
                BarButton(label = "Settings", onClick = onOpenSettings),
            ),
        )
    }
}

@Composable
private fun EmptyTodayState() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(gridUnitsAsDp(0.5f)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        LightText("No entry yet today", variant = LightTextVariant.Heading, align = androidx.compose.ui.text.style.TextAlign.Center, modifier = Modifier.fillMaxWidth())
        LightText(
            "Whenever you're ready, take a moment to capture where you are today.",
            variant = LightTextVariant.Paragraph,
            secondary = true,
            align = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun TodayEntryPreview(entry: JournalEntry, onRetake: () -> Unit) {
    Column(Modifier.fillMaxWidth()) {
        LightText(
            entry.date.format(DateTimeFormatter.ofPattern("EEEE, MMMM d")),
            variant = LightTextVariant.Subheading,
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = gridUnitsAsDp(1f)),
            horizontalArrangement = Arrangement.spacedBy(gridUnitsAsDp(0.5f)),
        ) {
            PhotoThumbnail(File(entry.rearPhotoPath), Modifier.weight(1f))
            PhotoThumbnail(File(entry.frontPhotoPath), Modifier.weight(1f))
        }
        if (entry.caption.isNotBlank()) {
            LightText(
                entry.caption,
                variant = LightTextVariant.Paragraph,
                modifier = Modifier.padding(top = gridUnitsAsDp(1f)),
            )
        }
    }
}

@Composable
fun PhotoThumbnail(file: File, modifier: Modifier = Modifier) {
    val bitmap = rememberSampledBitmap(file, reqSize = 512)
    Box(
        modifier = modifier.aspectRatio(1f).hairlineBorder(CandidTheme.colors.content),
        contentAlignment = Alignment.Center,
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }
    }
}
