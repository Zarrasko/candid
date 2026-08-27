package app.candid.storage.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface EntryDao {
    @Upsert
    suspend fun upsert(entry: EntryEntity)

    @Query("SELECT * FROM entries WHERE date = :date LIMIT 1")
    suspend fun findByDate(date: String): EntryEntity?

    @Query("SELECT * FROM entries ORDER BY date DESC")
    fun observeAll(): Flow<List<EntryEntity>>
}
