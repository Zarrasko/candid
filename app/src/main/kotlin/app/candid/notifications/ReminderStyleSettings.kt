package app.candid.notifications

import android.content.Context

enum class ReminderStyle { GENTLE, OVERLAY }

/** Isolates the gentle-vs-overlay reminder preference behind an interface, matching
 * [app.candid.capture.CaptureSettings]. */
interface ReminderStyleSettings {
    fun getStyle(): ReminderStyle
    fun setStyle(style: ReminderStyle)
}

class SharedPrefsReminderStyleSettings(context: Context) : ReminderStyleSettings {
    private val prefs = context.getSharedPreferences("candid_reminder_style", Context.MODE_PRIVATE)

    override fun getStyle(): ReminderStyle {
        val stored = prefs.getString(KEY_STYLE, null)
        return runCatching { ReminderStyle.valueOf(stored ?: "") }.getOrDefault(ReminderStyle.GENTLE)
    }

    override fun setStyle(style: ReminderStyle) {
        prefs.edit().putString(KEY_STYLE, style.name).apply()
    }

    companion object {
        private const val KEY_STYLE = "style"
    }
}
