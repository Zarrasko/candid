package app.candid.capture

import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
import java.io.File

enum class CameraLens { REAR, FRONT }

/** Isolates the actual camera platform API behind an interface — the only place a future
 * SDK-provided capture primitive would need to be swapped in. */
interface CameraController {
    fun bindPreview(lifecycleOwner: LifecycleOwner, previewView: PreviewView)
    fun setLens(lens: CameraLens)
    suspend fun capture(outputFile: File): Result<Unit>
    fun unbind()
}
