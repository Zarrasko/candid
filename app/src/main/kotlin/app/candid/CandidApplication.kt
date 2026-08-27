package app.candid

import android.app.Application
import app.candid.notifications.NotificationChannels

class CandidApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        NotificationChannels.ensureCreated(this)
        container.reminderScheduler.ensureScheduled()
    }
}
