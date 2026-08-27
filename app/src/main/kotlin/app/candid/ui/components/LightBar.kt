package app.candid.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.candid.theme.CandidTheme
import app.candid.theme.gridUnitsAsDp
import app.candid.theme.verticalGridUnitsAsDp

// The LP3's display has rounded corners that the OS masks over the rectangular
// framebuffer, so a border drawn flush to the true screen edge gets its corners clipped.
// This keeps full-bleed bars just inside that masked area.
private val screenEdgeMargin = 8.dp

// Bars can sit over a live camera preview or a colorful photo, not just the flat app
// background, so they need their own scrim behind the text to stay legible.
private const val BAR_SCRIM_ALPHA = 0.85f

data class BarButton(
    val label: String,
    val enabled: Boolean = true,
    val onClick: () -> Unit,
)

/** A 3-grid-unit-tall bar with left/center/right slots, matching Light's LightTopBar. */
@Composable
fun LightTopBar(
    modifier: Modifier = Modifier,
    left: BarButton? = null,
    title: String? = null,
    right: BarButton? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = screenEdgeMargin)
            .height(verticalGridUnitsAsDp(3f))
            .hairlineBorder(CandidTheme.colors.content)
            .background(CandidTheme.colors.background.copy(alpha = BAR_SCRIM_ALPHA))
            .padding(horizontal = gridUnitsAsDp(1f)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box {
            left?.let { BarButtonText(it) }
        }
        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
            title?.let { LightText(it, variant = LightTextVariant.Subheading, align = TextAlign.Center) }
        }
        Box(contentAlignment = Alignment.CenterEnd) {
            right?.let { BarButtonText(it) }
        }
    }
}

/** A 4-grid-unit-tall bar for up to 5 actions (3 if any carry a text label), matching LightBottomBar. */
@Composable
fun LightBottomBar(
    modifier: Modifier = Modifier,
    items: List<BarButton>,
) {
    require(items.size <= 5) { "LightBottomBar supports at most 5 items" }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = screenEdgeMargin)
            .height(verticalGridUnitsAsDp(4f))
            .hairlineBorder(CandidTheme.colors.content)
            .background(CandidTheme.colors.background.copy(alpha = BAR_SCRIM_ALPHA)),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        items.forEach { item ->
            Box(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                contentAlignment = Alignment.Center,
            ) {
                BarButtonText(item)
            }
        }
    }
}

/** A small standalone corner chip for a single action — for screens like the camera
 * preview where a full-width bar would be a lot of chrome for one button. */
@Composable
fun LightCornerButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .padding(screenEdgeMargin)
            .hairlineBorder(CandidTheme.colors.content)
            .background(CandidTheme.colors.background.copy(alpha = BAR_SCRIM_ALPHA))
            .lightClickable(onClick = onClick)
            .padding(horizontal = gridUnitsAsDp(1f), vertical = gridUnitsAsDp(0.5f)),
    ) {
        BasicText(
            text = label,
            style = CandidTheme.typography.button.copy(color = CandidTheme.colors.content),
            softWrap = false,
        )
    }
}

@Composable
private fun BarButtonText(button: BarButton) {
    val color = if (button.enabled) CandidTheme.colors.content else CandidTheme.colors.contentSecondary
    BasicText(
        text = button.label,
        modifier = Modifier.lightClickable(enabled = button.enabled, onClick = button.onClick),
        style = CandidTheme.typography.button.copy(color = color),
        softWrap = false,
    )
}
