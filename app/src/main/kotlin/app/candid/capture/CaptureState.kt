package app.candid.capture

import java.io.File

/** No timers anywhere in this state machine — every transition is user-driven except the
 * brief rear-to-front auto-advance right after a shot is taken. */
sealed class CaptureState {
    data object RearPreview : CaptureState()
    data object FrontPreview : CaptureState()
    data class Confirm(val rearFile: File, val frontFile: File, val caption: String = "") : CaptureState()
    data object Saving : CaptureState()
    data class Error(val message: String) : CaptureState()
}
