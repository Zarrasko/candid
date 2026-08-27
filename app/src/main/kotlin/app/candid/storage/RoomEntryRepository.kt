package app.candid.storage

import app.candid.domain.JournalEntry
import app.candid.storage.db.EntryDao
import app.candid.storage.db.EntryEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class RoomEntryRepository(private val dao: EntryDao) : EntryRepository {
    override suspend fun save(entry: JournalEntry) {
        dao.upsert(entry.toEntity())
    }

    override suspend fun findByDate(date: LocalDate): JournalEntry? =
        dao.findByDate(date.toString())?.toDomain()

    override fun observeAll(): Flow<List<JournalEntry>> =
        dao.observeAll().map { entities -> entities.map { it.toDomain() } }

    private fun JournalEntry.toEntity() = EntryEntity(
        date = date.toString(),
        rearPhotoPath = rearPhotoPath,
        frontPhotoPath = frontPhotoPath,
        caption = caption,
        capturedAtEpochMillis = capturedAtEpochMillis,
    )

    private fun EntryEntity.toDomain() = JournalEntry(
        date = LocalDate.parse(date),
        rearPhotoPath = rearPhotoPath,
        frontPhotoPath = frontPhotoPath,
        caption = caption,
        capturedAtEpochMillis = capturedAtEpochMillis,
    )
}
