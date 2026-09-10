package app.candid.capture

import java.io.File

/** Named by order, not by physical camera - [CaptureOrder] decides which lens is "first" vs
 * "second", so the same two states cover both rear-first and front-first sessions. Every
 * transition is user-driven except the brief auto-advance into the second capture. */
sealed class CaptureState {
    data object FirstPreview : CaptureState()
    data object SecondPreview : CaptureState()
    // Either file is null when its shot was skipped (manual mode only ever allows skipping
    // the second capture - the first is always mandatory).
    data class Confirm(val rearFile: File?, val frontFile: File?, val caption: String = "") : CaptureState()
    data object Saving : CaptureState()
    data class Error(val message: String) : CaptureState()
}
