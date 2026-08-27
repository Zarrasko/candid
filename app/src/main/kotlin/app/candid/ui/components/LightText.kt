package app.candid.ui.components

import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import app.candid.theme.CandidTheme
import app.candid.theme.LightTypography

enum class LightTextVariant {
    Title, Subtitle, Heading, Subheading, Copy, Button, Paragraph, ParagraphWide, Detail, Fine, Superfine, Micro
}

private fun LightTypography.styleFor(variant: LightTextVariant): TextStyle = when (variant) {
    LightTextVariant.Title -> title
    LightTextVariant.Subtitle -> subtitle
    LightTextVariant.Heading -> heading
    LightTextVariant.Subheading -> subheading
    LightTextVariant.Copy -> copy
    LightTextVariant.Button -> button
    LightTextVariant.Paragraph -> paragraph
    LightTextVariant.ParagraphWide -> paragraphWide
    LightTextVariant.Detail -> detail
    LightTextVariant.Fine -> fine
    LightTextVariant.Superfine -> superfine
    LightTextVariant.Micro -> micro
}

@Composable
fun LightText(
    text: String,
    modifier: Modifier = Modifier,
    variant: LightTextVariant = LightTextVariant.Copy,
    secondary: Boolean = false,
    underline: Boolean = false,
    align: TextAlign = TextAlign.Start,
) {
    val color = if (secondary) CandidTheme.colors.contentSecondary else CandidTheme.colors.content
    val baseStyle = CandidTheme.typography.styleFor(variant)
    BasicText(
        text = text,
        modifier = modifier,
        style = baseStyle.copy(
            color = color,
            textAlign = align,
            textDecoration = if (underline) TextDecoration.Underline else TextDecoration.None,
        ),
    )
}
