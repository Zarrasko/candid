package app.candid.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import app.candid.AppContainer

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        AppContainer(context).reminderScheduler.restoreAfterBoot()
    }
}
