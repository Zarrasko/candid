package app.candid.notifications

/** Isolates alarm/notification scheduling behind an interface — the only place a future
 * SDK-provided local-notification primitive would need to be swapped in. */
interface ReminderScheduler {
    /** Called once at app startup — arms the first alarm only if nothing is scheduled yet,
     * so repeated app opens don't keep re-randomizing an already-armed reminder. */
    fun ensureScheduled()

    /** Picks a random time within the daily window (skipping today if it's already past
     * or already captured) and arms the OS alarm for it. */
    fun scheduleNext()

    /** Re-arms the previously persisted trigger time after a device reboot, since
     * AlarmManager alarms don't survive one. Falls back to scheduleNext() if none exists. */
    fun restoreAfterBoot()

    /** The daily window reminders are randomized within, as [startHour, endHour) in 0-23. */
    fun getWindow(): Pair<Int, Int>

    /** Persists a new window and immediately re-arms the next alarm within it. */
    fun setWindow(startHour: Int, endHour: Int)

    /** Whether the OS will honor exact-time scheduling for this app right now. */
    fun hasExactAlarmPermission(): Boolean
}
