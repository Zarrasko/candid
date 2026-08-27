package app.candid.ui.screens

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
import app.candid.domain.JournalEntry
import app.candid.storage.EntryRepository
import app.candid.theme.gridUnitsAsDp
import app.candid.ui.components.BarButton
import app.candid.ui.components.LightBottomBar
import app.candid.ui.components.LightText
import app.candid.ui.components.LightTextField
import app.candid.ui.components.LightTextVariant
import app.candid.ui.components.LightTopBar
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun EntryDetailScreen(
    date: LocalDate,
    entryRepository: EntryRepository,
    onBack: () -> Unit,
) {
    var entry by remember(date) { mutableStateOf<JournalEntry?>(null) }
    var caption by remember(date) { mutableStateOf("") }
    val scope = rememberCoroutineScope()

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
                    PhotoThumbnail(File(current.frontPhotoPath), Modifier.weight(1f))
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
                BarButton(label = "Back", onClick = onBack),
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
