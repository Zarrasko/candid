package app.candid.storage.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

// v1 -> v2 made frontPhotoPath nullable (manual capture mode can skip the selfie). SQLite has
// no ALTER COLUMN for dropping a NOT NULL constraint, so this rebuilds the table the standard
// way: create the new shape, copy every row across, swap it in. A prior version of this
// migration used fallbackToDestructiveMigration instead, which silently wiped everyone's
// journal entries (photos survived on disk since only the DB rows were dropped) the first
// time a v1 install opened this database - never use that fallback for real user content
// again. No migration defined for some future version should fail loudly, not delete quietly.
private val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE entries_new (
                date TEXT NOT NULL PRIMARY KEY,
                rearPhotoPath TEXT NOT NULL,
                frontPhotoPath TEXT,
                caption TEXT NOT NULL,
                capturedAtEpochMillis INTEGER NOT NULL
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            INSERT INTO entries_new (date, rearPhotoPath, frontPhotoPath, caption, capturedAtEpochMillis)
            SELECT date, rearPhotoPath, frontPhotoPath, caption, capturedAtEpochMillis FROM entries
            """.trimIndent(),
        )
        db.execSQL("DROP TABLE entries")
        db.execSQL("ALTER TABLE entries_new RENAME TO entries")
    }
}

@Database(entities = [EntryEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun entryDao(): EntryDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun get(context: Context): AppDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "candid.db",
            )
                .addMigrations(MIGRATION_1_2)
                .build().also { instance = it }
        }
    }
}
