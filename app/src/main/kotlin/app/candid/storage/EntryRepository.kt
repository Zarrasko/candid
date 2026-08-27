package app.candid.storage

import app.candid.domain.JournalEntry
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/** Isolates persistence behind an interface — if a future sandboxed SDK exposes its own
 * storage primitive, only the implementation below needs to change. */
interface EntryRepository {
    suspend fun save(entry: JournalEntry)
    suspend fun findByDate(date: LocalDate): JournalEntry?
    fun observeAll(): Flow<List<JournalEntry>>
}
