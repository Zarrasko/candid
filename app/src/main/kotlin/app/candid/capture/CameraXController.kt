package app.candid.capture

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class CameraXController(private val context: Context) : CameraController {
    private val cameraController = LifecycleCameraController(context).apply {
        setEnabledUseCases(androidx.camera.view.CameraController.IMAGE_CAPTURE)
        imageCaptureFlashMode = ImageCapture.FLASH_MODE_OFF
    }

    override fun bindPreview(lifecycleOwner: LifecycleOwner, previewView: PreviewView) {
        previewView.controller = cameraController
        cameraController.bindToLifecycle(lifecycleOwner)
    }

    override fun setLens(lens: CameraLens) {
        cameraController.cameraSelector = when (lens) {
            CameraLens.REAR -> CameraSelector.DEFAULT_BACK_CAMERA
            CameraLens.FRONT -> CameraSelector.DEFAULT_FRONT_CAMERA
        }
    }

    override suspend fun capture(outputFile: File): Result<Unit> = suspendCoroutine { continuation ->
        val options = ImageCapture.OutputFileOptions.Builder(outputFile).build()
        cameraController.takePicture(
            options,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    continuation.resume(Result.success(Unit))
                }

                override fun onError(exception: ImageCaptureException) {
                    continuation.resume(Result.failure(exception))
                }
            },
        )
    }

    override fun unbind() {
        cameraController.unbind()
    }
}
