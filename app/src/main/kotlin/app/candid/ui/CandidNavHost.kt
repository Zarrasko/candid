package app.candid.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
fun CandidNavHost(container: AppContainer, captureRequestToken: Int) {
    var screen by remember { mutableStateOf<Screen>(if (captureRequestToken > 0) Screen.Capture else Screen.Home) }

    // MainActivity is singleTask - a reminder tap while it's already resident arrives as a
    // token bump here (via onNewIntent) rather than a fresh composition, so this has to be
    // reactive rather than just seeding the initial screen above.
    LaunchedEffect(captureRequestToken) {
        if (captureRequestToken > 0) screen = Screen.Capture
    }

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
            captureSettings = container.captureSettings,
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
            reminderStyleSettings = container.reminderStyleSettings,
            captureSettings = container.captureSettings,
            onBack = { screen = Screen.Home },
        )
    }
}
