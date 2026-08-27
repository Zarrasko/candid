package app.candid.theme

import android.content.ContentResolver
import android.provider.Settings
import android.util.Log

/**
 * LightOS runs the stock Android accessibility "Color correction" (Daltonizer) feature
 * enabled system-wide in monochromacy mode, which is how the device gets its grayscale
 * look — it's not a compositor effect baked into screenshots, so `adb screencap` never
 * shows it. Photos are the one thing worth seeing in real color, so while Candid is in the
 * foreground we temporarily disable it, then restore whatever state it was in on exit.
 *
 * Requires WRITE_SECURE_SETTINGS, a privileged permission normal runtime requests can't
 * grant — the user has to run:
 *   adb shell pm grant app.candid android.permission.WRITE_SECURE_SETTINGS
 * Without it, this silently no-ops and the app just stays grayscale like everything else.
 */
object DisplayColorOverride {
    private const val TAG = "DisplayColorOverride"
    private const val KEY_ENABLED = "accessibility_display_daltonizer_enabled"
    private const val KEY_MODE = "accessibility_display_daltonizer"
    private const val MODE_MONOCHROMACY = 0

    private var restoreEnabled: Int? = null
    private var restoreMode: Int? = null

    fun enableColor(resolver: ContentResolver) {
        val enabled = Settings.Secure.getInt(resolver, KEY_ENABLED, 0)
        val mode = Settings.Secure.getInt(resolver, KEY_MODE, -1)
        val isGrayscale = enabled == 1 && mode == MODE_MONOCHROMACY
        if (!isGrayscale) return

        restoreEnabled = enabled
        restoreMode = mode
        try {
            Settings.Secure.putInt(resolver, KEY_ENABLED, 0)
        } catch (e: SecurityException) {
            Log.w(TAG, "WRITE_SECURE_SETTINGS not granted — run: adb shell pm grant app.candid android.permission.WRITE_SECURE_SETTINGS")
            restoreEnabled = null
            restoreMode = null
        }
    }

    fun restoreGrayscale(resolver: ContentResolver) {
        val enabled = restoreEnabled ?: return
        val mode = restoreMode ?: return
        try {
            Settings.Secure.putInt(resolver, KEY_MODE, mode)
            Settings.Secure.putInt(resolver, KEY_ENABLED, enabled)
        } catch (e: SecurityException) {
            Log.w(TAG, "Couldn't restore grayscale — permission was revoked mid-session")
        }
        restoreEnabled = null
        restoreMode = null
    }
}
