package app.candid

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import app.candid.capture.HardwareCaptureButton
import app.candid.theme.CandidTheme
import app.candid.theme.DisplayColorOverride
import app.candid.ui.CandidNavHost

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val container = (application as CandidApplication).container
        val startOnCapture = intent?.action == ACTION_OPEN_CAPTURE

        setContent {
            CandidTheme {
                Box(
                    modifier = Modifier.fillMaxSize().background(CandidTheme.colors.background),
                ) {
                    CandidNavHost(container = container, startOnCapture = startOnCapture)
                }
            }
        }
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
