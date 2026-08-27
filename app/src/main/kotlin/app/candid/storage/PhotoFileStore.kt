package app.candid.storage

import android.content.Context
import java.io.File
import java.time.LocalDate

/** Photos are private journal content, not shareable media — stored in app-private
 * storage (never MediaStore/shared gallery), matching the light-botany precedent. */
interface PhotoFileStore {
    fun fileFor(date: LocalDate, slot: PhotoSlot): File
    fun delete(date: LocalDate, slot: PhotoSlot)
}

enum class PhotoSlot { REAR, FRONT }

class FilesDirPhotoStore(context: Context) : PhotoFileStore {
    private val directory = File(context.filesDir, "journal_photos").apply { mkdirs() }

    override fun fileFor(date: LocalDate, slot: PhotoSlot): File =
        File(directory, "${date}_${slot.name.lowercase()}.jpg")

    override fun delete(date: LocalDate, slot: PhotoSlot) {
        fileFor(date, slot).delete()
    }
}
