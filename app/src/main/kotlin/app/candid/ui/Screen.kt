package app.candid.ui

import java.time.LocalDate

sealed class Screen {
    data object Home : Screen()
    data object Capture : Screen()
    data object History : Screen()
    data class EntryDetail(val date: LocalDate) : Screen()
    data object Settings : Screen()
}
