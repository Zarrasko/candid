package app.candid.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import app.candid.AppContainer
import app.candid.ui.screens.CaptureScreen
import app.candid.ui.screens.EntryDetailScreen
import app.candid.ui.screens.HistoryScreen
import app.candid.ui.screens.HomeScreen
import app.candid.ui.screens.SettingsScreen

@Composable
fun CandidNavHost(container: AppContainer, startOnCapture: Boolean) {
    var screen by remember { mutableStateOf<Screen>(if (startOnCapture) Screen.Capture else Screen.Home) }

    when (val current = screen) {
        Screen.Home -> HomeScreen(
            entryRepository = container.entryRepository,
            onCapture = { screen = Screen.Capture },
            onOpenHistory = { screen = Screen.History },
            onOpenSettings = { screen = Screen.Settings },
        )

        Screen.Capture -> CaptureScreen(
            photoFileStore = container.photoFileStore,
            entryRepository = container.entryRepository,
            onDone = { screen = Screen.Home },
            onCancel = { screen = Screen.Home },
        )

        Screen.History -> HistoryScreen(
            entryRepository = container.entryRepository,
            onBack = { screen = Screen.Home },
            onOpenEntry = { date -> screen = Screen.EntryDetail(date) },
        )

        is Screen.EntryDetail -> EntryDetailScreen(
            date = current.date,
            entryRepository = container.entryRepository,
            onBack = { screen = Screen.History },
        )

        Screen.Settings -> SettingsScreen(
            reminderScheduler = container.reminderScheduler,
            onBack = { screen = Screen.Home },
        )
    }
}
