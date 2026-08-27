package app.candid.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context

object NotificationChannels {
    const val REMINDER_CHANNEL_ID = "daily_reminder"

    fun ensureCreated(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            REMINDER_CHANNEL_ID,
            "Daily reminder",
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = "The once-a-day prompt to capture today's photo"
        }
        manager.createNotificationChannel(channel)
    }
}
