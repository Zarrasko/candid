package app.candid

import android.content.Context
import app.candid.notifications.AlarmReminderScheduler
import app.candid.notifications.ReminderScheduler
import app.candid.storage.EntryRepository
import app.candid.storage.FilesDirPhotoStore
import app.candid.storage.PhotoFileStore
import app.candid.storage.RoomEntryRepository
import app.candid.storage.db.AppDatabase

/** Simple manual dependency wiring — no DI framework needed for an app this size. */
class AppContainer(context: Context) {
    val entryRepository: EntryRepository = RoomEntryRepository(AppDatabase.get(context).entryDao())
    val photoFileStore: PhotoFileStore = FilesDirPhotoStore(context)
    val reminderScheduler: ReminderScheduler = AlarmReminderScheduler(context)
}
