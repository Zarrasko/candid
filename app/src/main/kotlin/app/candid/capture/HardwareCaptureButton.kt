package app.candid.capture

/** MainActivity intercepts the LP3's physical camera key (KEYCODE_CAMERA) and forwards it
 * here, since Compose doesn't see hardware key events by default. Whichever capture step is
 * currently on screen registers its own capture action as this callback while active. */
object HardwareCaptureButton {
    var onPressed: (() -> Unit)? = null
}
