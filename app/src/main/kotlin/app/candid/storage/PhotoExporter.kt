package app.candid.storage

import android.content.ContentValues
import android.content.Context
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.time.LocalDate

/** Journal photos otherwise never leave app-private storage (see [PhotoFileStore]) - this is
 * the one deliberate, opt-in exception, triggered only by an explicit Export action (never
 * automatically) so a copy can land in the system Photos app for sharing/backup. */
interface PhotoExporter {
    /** Copies [file] into the system Pictures/Candid album via MediaStore. Returns true on
     * success. Safe to call from a background thread. */
    fun exportToPhotos(file: File, date: LocalDate, slot: PhotoSlot): Boolean
}

class MediaStorePhotoExporter(private val context: Context) : PhotoExporter {
    override fun exportToPhotos(file: File, date: LocalDate, slot: PhotoSlot): Boolean {
        if (!file.exists()) return false
        val resolver = context.contentResolver

        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "candid_${date}_${slot.name.lowercase()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/Candid")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }

        val collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val uri = resolver.insert(collection, values) ?: return false

        val copied = runCatching {
            resolver.openOutputStream(uri)?.use { out -> file.inputStream().use { it.copyTo(out) } }
        }.isSuccess

        if (!copied) {
            runCatching { resolver.delete(uri, null, null) }
            return false
        }

        values.clear()
        values.put(MediaStore.Images.Media.IS_PENDING, 0)
        resolver.update(uri, values, null, null)
        return true
    }
}
