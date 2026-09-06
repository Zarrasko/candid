package app.candid

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import app.candid.capture.HardwareCaptureButton
import app.candid.theme.CandidTheme
import app.candid.theme.DisplayColorOverride
import app.candid.ui.CandidNavHost

class MainActivity : ComponentActivity() {
    // MainActivity is singleTask, so a reminder tap while it's already resident delivers
    // through onNewIntent rather than a fresh onCreate - a plain onCreate-only read of
    // intent.action would silently drop that deep link. This token is bumped on every
    // ACTION_OPEN_CAPTURE intent (cold or warm) so Compose can react to it directly.
    private var captureRequestToken by mutableIntStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val container = (application as CandidApplication).container
        if (intent?.action == ACTION_OPEN_CAPTURE) captureRequestToken++

        setContent {
            CandidTheme {
                Box(
                    modifier = Modifier.fillMaxSize().background(CandidTheme.colors.background),
                ) {
                    CandidNavHost(container = container, captureRequestToken = captureRequestToken)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.action == ACTION_OPEN_CAPTURE) captureRequestToken++
    }

    override fun onResume() {
        super.onResume()
        DisplayColorOverride.enableColor(contentResolver)
    }

    override fun onPause() {
        DisplayColorOverride.restoreGrayscale(contentResolver)
        super.onPause()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_CAMERA && event.repeatCount == 0) {
            val handled = HardwareCaptureButton.onPressed
            if (handled != null) {
                handled()
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    companion object {
        const val ACTION_OPEN_CAPTURE = "app.candid.action.OPEN_CAPTURE"
    }
}
