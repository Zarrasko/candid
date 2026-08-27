package app.candid.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.random.Random

class AlarmReminderScheduler(private val context: Context) : ReminderScheduler {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("candid_reminders", Context.MODE_PRIVATE)
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    override fun ensureScheduled() {
        val persisted = prefs.getLong(KEY_NEXT_TRIGGER_MILLIS, -1L)
        if (persisted <= System.currentTimeMillis()) {
            scheduleNext()
        } else {
            arm(persisted)
        }
    }

    override fun scheduleNext() {
        val now = LocalDateTime.now()
        val startHour = prefs.getInt(KEY_WINDOW_START_HOUR, DEFAULT_WINDOW_START_HOUR)
        val endHour = prefs.getInt(KEY_WINDOW_END_HOUR, DEFAULT_WINDOW_END_HOUR)

        val targetDate = if (now.hour < startHour) LocalDate.now() else LocalDate.now().plusDays(1)
        val windowStart = targetDate.atTime(startHour, 0)
        val windowEnd = targetDate.atTime(endHour, 0)

        val zone = ZoneId.systemDefault()
        val triggerMillis = Random.nextLong(
            windowStart.atZone(zone).toInstant().toEpochMilli(),
            windowEnd.atZone(zone).toInstant().toEpochMilli(),
        )

        prefs.edit().putLong(KEY_NEXT_TRIGGER_MILLIS, triggerMillis).apply()
        arm(triggerMillis)
    }

    override fun restoreAfterBoot() {
        val persisted = prefs.getLong(KEY_NEXT_TRIGGER_MILLIS, -1L)
        if (persisted > System.currentTimeMillis()) {
            arm(persisted)
        } else {
            scheduleNext()
        }
    }

    private fun arm(triggerMillis: Long) {
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            Intent(context, ReminderReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        try {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
        } catch (_: SecurityException) {
            // Exact-alarm permission not granted — fall back to an inexact alarm rather
            // than crash; the reminder will still arrive close to the chosen random time.
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
        }
    }

    override fun getWindow(): Pair<Int, Int> = Pair(
        prefs.getInt(KEY_WINDOW_START_HOUR, DEFAULT_WINDOW_START_HOUR),
        prefs.getInt(KEY_WINDOW_END_HOUR, DEFAULT_WINDOW_END_HOUR),
    )

    override fun setWindow(startHour: Int, endHour: Int) {
        require(startHour in 0..23 && endHour in 0..23 && startHour < endHour) {
            "Invalid window: $startHour-$endHour"
        }
        prefs.edit()
            .putInt(KEY_WINDOW_START_HOUR, startHour)
            .putInt(KEY_WINDOW_END_HOUR, endHour)
            .apply()
        scheduleNext()
    }

    override fun hasExactAlarmPermission(): Boolean = alarmManager.canScheduleExactAlarms()

    companion object {
        private const val REQUEST_CODE = 1001
        private const val KEY_NEXT_TRIGGER_MILLIS = "next_trigger_millis"
        private const val KEY_WINDOW_START_HOUR = "window_start_hour"
        private const val KEY_WINDOW_END_HOUR = "window_end_hour"
        private const val DEFAULT_WINDOW_START_HOUR = 9
        private const val DEFAULT_WINDOW_END_HOUR = 21
    }
}
