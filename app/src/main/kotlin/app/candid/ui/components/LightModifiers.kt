package app.candid.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** Light's flat aesthetic uses a 1dp hairline border instead of elevation or shadows. */
fun Modifier.hairlineBorder(color: Color): Modifier = border(BorderStroke(1.dp, color))

/** No ripple, no default indication — matches Light's flat tap feedback. */
@Composable
fun Modifier.lightClickable(enabled: Boolean = true, onClick: () -> Unit): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    return clickable(
        interactionSource = interactionSource,
        indication = null,
        enabled = enabled,
        onClick = onClick,
    )
}
