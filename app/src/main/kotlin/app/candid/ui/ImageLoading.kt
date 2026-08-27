package app.candid.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/** Journal photos come straight off the camera at full sensor resolution, so we downsample
 * before decoding rather than loading full-size bitmaps into memory for thumbnails/previews. */
fun loadSampledBitmap(file: File, reqSize: Int): Bitmap? {
    if (!file.exists()) return null
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(file.absolutePath, bounds)

    var sampleSize = 1
    var height = bounds.outHeight
    var width = bounds.outWidth
    while (height / 2 >= reqSize && width / 2 >= reqSize) {
        height /= 2
        width /= 2
        sampleSize *= 2
    }

    val options = BitmapFactory.Options().apply { inSampleSize = sampleSize }
    return BitmapFactory.decodeFile(file.absolutePath, options)
}

@Composable
fun rememberSampledBitmap(file: File, reqSize: Int): Bitmap? {
    val state = produceState<Bitmap?>(initialValue = null, file.absolutePath, file.lastModified()) {
        value = withContext(Dispatchers.IO) { loadSampledBitmap(file, reqSize) }
    }
    return state.value
}
