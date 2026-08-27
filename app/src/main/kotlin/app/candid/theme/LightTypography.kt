package app.candid.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Light's SDK loads "Akkurat" (a licensed Lineto font it cannot redistribute to third
// parties) with a FontFamily.Default fallback. Since we can't obtain that license either,
// we use the system default sans-serif directly rather than bundling a substitute.
val CandidFontFamily = FontFamily.Default

data class LightTypography(
    val title: TextStyle,
    val subtitle: TextStyle,
    val heading: TextStyle,
    val subheading: TextStyle,
    val copy: TextStyle,
    val button: TextStyle,
    val paragraph: TextStyle,
    val paragraphWide: TextStyle,
    val detail: TextStyle,
    val fine: TextStyle,
    val superfine: TextStyle,
    val micro: TextStyle,
)

@Composable
fun buildLightTypography(): LightTypography {
    @Composable fun style(sizePx: Float, weight: FontWeight, lineHeightMultiplier: Float, letterSpacingEm: Float = 0f) = TextStyle(
        fontFamily = CandidFontFamily,
        fontWeight = weight,
        fontSize = sizePx.designVerticalPxToSp(),
        lineHeight = (sizePx * lineHeightMultiplier).designVerticalPxToSp(),
        letterSpacing = (sizePx * letterSpacingEm).designVerticalPxToSp(),
    )

    return LightTypography(
        title = style(115f, FontWeight.Light, 1.10f),
        subtitle = style(52f, FontWeight.Normal, 1.20f),
        heading = style(38f, FontWeight.Normal, 1.35f),
        subheading = style(30f, FontWeight.Normal, 1.25f, letterSpacingEm = 0.03f),
        copy = style(30f, FontWeight.Normal, 1.50f),
        button = style(30f, FontWeight.Medium, 1.10f, letterSpacingEm = 0.15f),
        paragraph = style(24.5f, FontWeight.Normal, 1.25f),
        paragraphWide = style(25f, FontWeight.Normal, 1.30f, letterSpacingEm = 0.02f),
        detail = style(20f, FontWeight.Normal, 1.45f),
        fine = style(25f, FontWeight.Normal, 1.15f, letterSpacingEm = 0.03f),
        superfine = style(16f, FontWeight.Normal, 1.20f),
        micro = style(8f, FontWeight.Normal, 1.20f),
    )
}
