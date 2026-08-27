package app.candid.capture

import android.view.ViewGroup
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun CameraPreview(cameraController: CameraController, modifier: Modifier = Modifier) {
    val lifecycleOwner = LocalLifecycleOwner.current
    AndroidView(
        modifier = modifier,
        factory = { context ->
            PreviewView(context).also { previewView ->
                // Without an explicit MATCH_PARENT layoutParams, PreviewView measures
                // itself against the camera's native resolution instead of the space
                // Compose gave it, so it overflows its bounds and covers the top bar.
                previewView.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                )
                // COMPATIBLE forces TextureView instead of SurfaceView, so the preview
                // composites correctly within the surrounding Compose layout (a plain
                // SurfaceView can draw on top of sibling Compose content, hiding the bars).
                previewView.implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                cameraController.bindPreview(lifecycleOwner, previewView)
            }
        },
    )
}
