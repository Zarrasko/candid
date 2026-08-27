package app.candid.domain

import java.time.LocalDate

/** One journal entry per calendar day. Pure Kotlin — no Android imports, so this is the
 * most portable piece if the app is ever migrated onto a future sandboxed SDK. */
data class JournalEntry(
    val date: LocalDate,
    val rearPhotoPath: String,
    val frontPhotoPath: String,
    val caption: String,
    val capturedAtEpochMillis: Long,
)
