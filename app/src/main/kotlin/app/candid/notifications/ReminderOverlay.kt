package app.candid.notifications

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.VibratorManager
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import android.graphics.PixelFormat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import app.candid.MainActivity
import app.candid.theme.CandidTheme
import app.candid.theme.gridUnitsAsDp
import app.candid.ui.components.LightText
import app.candid.ui.components.LightTextVariant
import app.candid.ui.components.hairlineBorder
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner

private const val SWIPE_UP_DISMISS_THRESHOLD_PX = -60f

// A ComposeView added directly via WindowManager has no Activity behind it, so it has none of
// the owners Compose needs (lifecycle, saved state, view model store) - this manufactures
// minimal ones. Ported from the Light SDK's LightOverlay, which uses the identical pattern.
private class OverlayLifecycleOwner : LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {
    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val viewModelStore: ViewModelStore = ViewModelStore()
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    fun start() {
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
        lifecycleRegistry.currentState = Lifecycle.State.STARTED
        lifecycleRegistry.currentState = Lifecycle.State.RESUMED
    }

    fun destroy() {
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
    }
}

/**
 * A heads-up-style alert that draws directly over whatever the user is doing, for the LightOS
 * builds where the standard notification is nearly invisible (no reachable shade — just an
 * asterisk next to the clock). Ported from the Light SDK's `LightOverlay` (built for Agenda's
 * identical reminder-visibility problem), reskinned in Candid's own flat/hairline look instead
 * of that version's rounded Material3 card. Needs `android.permission.SYSTEM_ALERT_WINDOW`,
 * which has no one-tap runtime prompt (see [rememberOverlayPermissionRequester]).
 */
object ReminderOverlay {
    // The one currently-showing overlay, if any - a fresh show() replaces it rather than
    // stacking a second box on top.
    private var active: ActiveOverlay? = null

    private class ActiveOverlay(val windowManager: WindowManager, val composeView: ComposeView, val owner: OverlayLifecycleOwner) {
        var removed = false
        fun remove() {
            if (removed) return
            removed = true
            runCatching { windowManager.removeViewImmediate(composeView) }
            owner.destroy()
        }
    }

    /** Whether "Display over other apps" is currently granted. */
    fun canShow(context: Context): Boolean = Settings.canDrawOverlays(context)

    /**
     * Shows a small bordered card near the top of the screen - it stays until the user taps it
     * (reopens the app) or swipes it up (dismisses), and never times out on its own. Replaces
     * any overlay from a previous [show] call that's still up. Also fires a single short
     * vibration. No-ops if the permission isn't granted.
     *
     * Only bypasses the lock screen ([WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD]) when
     * the device has no PIN/pattern/biometric lock set at all ([KeyguardManager.isKeyguardSecure]
     * is false) - on a device with real lock security, that flag would be a genuine security
     * regression, so the card only shows *behind* the lock screen there instead.
     */
    fun show(context: Context, title: String, text: String) {
        if (!canShow(context)) return
        val mainHandler = Handler(Looper.getMainLooper())

        mainHandler.post {
            active?.remove()
            active = null

            val windowManager = context.getSystemService(WindowManager::class.java)
            val keyguardManager = context.getSystemService(KeyguardManager::class.java)
            val owner = OverlayLifecycleOwner().apply { start() }

            lateinit var overlay: ActiveOverlay

            fun removeIfCurrent() {
                if (active === overlay) active = null
                overlay.remove()
            }

            fun openApp() {
                // Deep-links straight to capture, matching what tapping the Gentle-style
                // notification already does - the reminder has exactly one purpose.
                val launchIntent = Intent(context, MainActivity::class.java).apply {
                    action = MainActivity.ACTION_OPEN_CAPTURE
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                context.startActivity(launchIntent)
                removeIfCurrent()
            }

            val composeView = ComposeView(context).apply {
                setViewTreeLifecycleOwner(owner)
                setViewTreeViewModelStoreOwner(owner)
                setViewTreeSavedStateRegistryOwner(owner)
                setContent {
                    CandidTheme(darkTheme = true) {
                        OverlayCard(
                            title = title,
                            text = text,
                            onTap = ::openApp,
                            onSwipeDismiss = ::removeIfCurrent,
                        )
                    }
                }
            }

            val canBypassKeyguard = keyguardManager?.isKeyguardSecure == false
            var flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
            if (canBypassKeyguard) {
                flags = flags or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
            }

            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                flags,
                PixelFormat.TRANSLUCENT,
            ).apply {
                gravity = Gravity.TOP
                y = 24
            }

            overlay = ActiveOverlay(windowManager, composeView, owner)
            runCatching { windowManager.addView(composeView, params) }
                .onFailure { owner.destroy(); return@post }
            active = overlay

            runCatching {
                val vibrator = context.getSystemService(VibratorManager::class.java).defaultVibrator
                vibrator.vibrate(VibrationEffect.createOneShot(250, VibrationEffect.DEFAULT_AMPLITUDE))
            }
        }
    }
}

@Composable
private fun OverlayCard(
    title: String,
    text: String,
    onTap: () -> Unit,
    onSwipeDismiss: () -> Unit,
) {
    var dragAccumulator = 0f
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = gridUnitsAsDp(1f))
            .background(CandidTheme.colors.background)
            .hairlineBorder(CandidTheme.colors.content)
            .padding(gridUnitsAsDp(1f))
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragStart = { dragAccumulator = 0f },
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        dragAccumulator += dragAmount
                        if (dragAccumulator < SWIPE_UP_DISMISS_THRESHOLD_PX) onSwipeDismiss()
                    },
                )
            }
            .pointerInput(Unit) {
                detectTapGestures(onTap = { onTap() })
            },
    ) {
        LightText(text = title, variant = LightTextVariant.Subheading)
        LightText(
            text = text,
            variant = LightTextVariant.Paragraph,
            secondary = true,
            modifier = Modifier.padding(top = gridUnitsAsDp(0.25f)),
        )
    }
}

/**
 * Returns a function that opens the system "Display over other apps" settings screen for
 * Candid. Unlike a notification permission request, there's no dialog and no reliable
 * granted/denied result code - [onResult] fires when the user returns to the app, re-checking
 * [ReminderOverlay.canShow] directly rather than trusting the activity result.
 */
@Composable
fun rememberOverlayPermissionRequester(onResult: (Boolean) -> Unit = {}): () -> Unit {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { onResult(Settings.canDrawOverlays(context)) }

    return {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${context.packageName}"),
        )
        launcher.launch(intent)
    }
}
