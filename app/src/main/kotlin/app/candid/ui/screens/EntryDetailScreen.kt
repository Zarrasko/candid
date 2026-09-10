package app.candid.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import app.candid.domain.JournalEntry
import app.candid.storage.EntryRepository
import app.candid.storage.PhotoExporter
import app.candid.storage.PhotoSlot
import app.candid.theme.gridUnitsAsDp
import app.candid.ui.components.BarButton
import app.candid.ui.components.LightBottomBar
import app.candid.ui.components.LightText
import app.candid.ui.components.LightTextField
import app.candid.ui.components.LightTextVariant
import app.candid.ui.components.LightTopBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun EntryDetailScreen(
    date: LocalDate,
    entryRepository: EntryRepository,
    photoExporter: PhotoExporter,
    onBack: () -> Unit,
) {
    var entry by remember(date) { mutableStateOf<JournalEntry?>(null) }
    var caption by remember(date) { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(date) {
        entry = entryRepository.findByDate(date)
        caption = entry?.caption.orEmpty()
    }

    Column(Modifier.fillMaxSize()) {
        LightTopBar(
            left = BarButton(label = "Back", onClick = onBack),
            title = date.format(DateTimeFormatter.ofPattern("EEEE, MMM d")),
        )

        val current = entry
        if (current == null) {
            Column(Modifier.weight(1f).fillMaxWidth().padding(gridUnitsAsDp(1f))) {
                LightText("Loading...", variant = LightTextVariant.Paragraph, secondary = true)
            }
        } else {
            Column(Modifier.weight(1f).fillMaxWidth().padding(gridUnitsAsDp(1f))) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(gridUnitsAsDp(0.5f)),
                ) {
                    PhotoThumbnail(File(current.rearPhotoPath), Modifier.weight(1f))
                    PhotoThumbnail(current.frontPhotoPath?.let(::File), Modifier.weight(1f))
                }
                LightText(
                    "Caption",
                    variant = LightTextVariant.Superfine,
                    secondary = true,
                    modifier = Modifier.padding(top = gridUnitsAsDp(1f), bottom = gridUnitsAsDp(0.25f)),
                )
                LightTextField(
                    value = caption,
                    onValueChange = { caption = it },
                    placeholder = "Add a note about today...",
                )
            }
        }

        LightBottomBar(
            items = listOf(
                BarButton(
                    label = "Export",
                    enabled = entry != null,
                    onClick = {
                        entry?.let { savedEntry ->
                            scope.launch {
                                val exported = withContext(Dispatchers.IO) {
                                    var ok = photoExporter.exportToPhotos(
                                        File(savedEntry.rearPhotoPath),
                                        savedEntry.date,
                                        PhotoSlot.REAR,
                                    )
                                    savedEntry.frontPhotoPath?.let { frontPath ->
                                        ok = photoExporter.exportToPhotos(
                                            File(frontPath),
                                            savedEntry.date,
                                            PhotoSlot.FRONT,
                                        ) && ok
                                    }
                                    ok
                                }
                                Toast.makeText(
                                    context,
                                    if (exported) "Saved to Photos" else "Couldn't save to Photos",
                                    Toast.LENGTH_SHORT,
                                ).show()
                            }
                        }
                    },
                ),
                BarButton(
                    label = "Save",
                    enabled = entry != null,
                    onClick = {
                        entry?.let { savedEntry ->
                            scope.launch {
                                entryRepository.save(savedEntry.copy(caption = caption))
                                onBack()
                            }
                        }
                    },
                ),
            ),
        )
    }
}
