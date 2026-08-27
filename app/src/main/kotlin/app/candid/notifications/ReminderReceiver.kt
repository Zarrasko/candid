package app.candid.notifications

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import app.candid.AppContainer
import app.candid.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val container = AppContainer(context)
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val alreadyCaptured = container.entryRepository.findByDate(LocalDate.now()) != null
                if (!alreadyCaptured) {
                    postNotification(context)
                }
                container.reminderScheduler.scheduleNext()
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun postNotification(context: Context) {
        NotificationChannels.ensureCreated(context)

        val openCaptureIntent = Intent(context, MainActivity::class.java).apply {
            action = MainActivity.ACTION_OPEN_CAPTURE
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentIntent = PendingIntent.getActivity(
            context,
            0,
            openCaptureIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, NotificationChannels.REMINDER_CHANNEL_ID)
            .setSmallIcon(app.candid.R.drawable.ic_notification)
            .setContentTitle("Today's photo")
            .setContentText("Take a moment to capture where you are today.")
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .build()

        val canPost = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
        if (canPost) {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        }
    }

    companion object {
        private const val NOTIFICATION_ID = 2001
    }
}
