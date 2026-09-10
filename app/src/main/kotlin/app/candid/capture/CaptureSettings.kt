package app.candid.capture

import android.content.Context

/** Which physical camera fires first in a dual-shot session - the other fires second, and
 * only the second one is ever skippable (see [CaptureSettings.isAutoDualCaptureEnabled]). */
enum class CaptureOrder { REAR_FIRST, FRONT_FIRST }

/** Isolates capture-flow preferences behind an interface, matching the pattern used for
 * [CameraController] and [app.candid.notifications.ReminderScheduler]. */
interface CaptureSettings {
    /** True (the default): the first capture auto-advances to the second with no second
     * tap. False: after the first shot, the second viewfinder waits for either a capture or
     * a skip, so the second shot is never forced. */
    fun isAutoDualCaptureEnabled(): Boolean
    fun setAutoDualCaptureEnabled(enabled: Boolean)

    /** Which camera fires first. Defaults to [CaptureOrder.REAR_FIRST]. */
    fun getCaptureOrder(): CaptureOrder
    fun setCaptureOrder(order: CaptureOrder)
}

class SharedPrefsCaptureSettings(context: Context) : CaptureSettings {
    private val prefs = context.getSharedPreferences("candid_capture_settings", Context.MODE_PRIVATE)

    override fun isAutoDualCaptureEnabled(): Boolean = prefs.getBoolean(KEY_AUTO_DUAL_CAPTURE, true)

    override fun setAutoDualCaptureEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_DUAL_CAPTURE, enabled).apply()
    }

    override fun getCaptureOrder(): CaptureOrder {
        val stored = prefs.getString(KEY_CAPTURE_ORDER, null)
        return runCatching { CaptureOrder.valueOf(stored ?: "") }.getOrDefault(CaptureOrder.REAR_FIRST)
    }

    override fun setCaptureOrder(order: CaptureOrder) {
        prefs.edit().putString(KEY_CAPTURE_ORDER, order.name).apply()
    }

    companion object {
        private const val KEY_AUTO_DUAL_CAPTURE = "auto_dual_capture"
        private const val KEY_CAPTURE_ORDER = "capture_order"
    }
}
