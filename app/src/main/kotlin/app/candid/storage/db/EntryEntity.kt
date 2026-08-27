package app.candid.storage.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "entries")
data class EntryEntity(
    @PrimaryKey val date: String, // ISO-8601, e.g. "2026-08-27"
    val rearPhotoPath: String,
    val frontPhotoPath: String,
    val caption: String,
    val capturedAtEpochMillis: Long,
)
