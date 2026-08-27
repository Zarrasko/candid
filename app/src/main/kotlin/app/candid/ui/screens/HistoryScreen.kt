package app.candid.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.candid.domain.JournalEntry
import app.candid.storage.EntryRepository
import app.candid.theme.gridUnitsAsDp
import app.candid.ui.components.BarButton
import app.candid.ui.components.LightBottomBar
import app.candid.ui.components.LightText
import app.candid.ui.components.LightTextVariant
import app.candid.ui.components.LightTopBar
import app.candid.ui.components.hairlineBorder
import app.candid.ui.components.lightClickable
import app.candid.theme.CandidTheme
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun HistoryScreen(
    entryRepository: EntryRepository,
    onBack: () -> Unit,
    onOpenEntry: (LocalDate) -> Unit,
) {
    val entries by entryRepository.observeAll().collectAsState(initial = emptyList())

    Column(Modifier.fillMaxSize()) {
        LightTopBar(left = BarButton(label = "Back", onClick = onBack), title = "History")

        if (entries.isEmpty()) {
            Column(
                modifier = Modifier.weight(1f).fillMaxWidth().padding(gridUnitsAsDp(1f)),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                LightText("No entries yet", variant = LightTextVariant.Paragraph, secondary = true, align = TextAlign.Center)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 8.dp),
            ) {
                items(entries, key = { it.date.toString() }) { entry ->
                    HistoryRow(entry, onClick = { onOpenEntry(entry.date) })
                }
            }
        }

        LightBottomBar(items = listOf(BarButton(label = "Back", onClick = onBack)))
    }
}

@Composable
private fun HistoryRow(entry: JournalEntry, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .hairlineBorder(CandidTheme.colors.contentSecondary)
            .lightClickable(onClick = onClick)
            .padding(gridUnitsAsDp(0.5f)),
        horizontalArrangement = Arrangement.spacedBy(gridUnitsAsDp(0.5f)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PhotoThumbnail(File(entry.rearPhotoPath), Modifier.size(gridUnitsAsDp(4f)))
        Column(Modifier.weight(1f)) {
            LightText(entry.date.format(DateTimeFormatter.ofPattern("EEE, MMM d")), variant = LightTextVariant.Detail)
            if (entry.caption.isNotBlank()) {
                LightText(entry.caption, variant = LightTextVariant.Superfine, secondary = true)
            }
        }
    }
}
