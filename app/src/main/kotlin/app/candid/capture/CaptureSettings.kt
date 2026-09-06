package app.candid.capture

import android.content.Context

/** Isolates the auto-vs-manual dual-shot preference behind an interface, matching the
 * pattern used for [CameraController] and [app.candid.notifications.ReminderScheduler]. */
interface CaptureSettings {
    /** True (the default): rear capture auto-advances to a front capture with no second
     * tap. False: after the rear shot, the front viewfinder waits for either a capture or
     * a skip, so a selfie is never forced. */
    fun isAutoDualCaptureEnabled(): Boolean
    fun setAutoDualCaptureEnabled(enabled: Boolean)
}

class SharedPrefsCaptureSettings(context: Context) : CaptureSettings {
    private val prefs = context.getSharedPreferences("candid_capture_settings", Context.MODE_PRIVATE)

    override fun isAutoDualCaptureEnabled(): Boolean = prefs.getBoolean(KEY_AUTO_DUAL_CAPTURE, true)

    override fun setAutoDualCaptureEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_DUAL_CAPTURE, enabled).apply()
    }

    companion object {
        private const val KEY_AUTO_DUAL_CAPTURE = "auto_dual_capture"
    }
}
