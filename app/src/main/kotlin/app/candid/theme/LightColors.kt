package app.candid.theme

import androidx.compose.ui.graphics.Color

data class LightColors(
    val background: Color,
    val content: Color,
    val contentSecondary: Color,
)

val DarkLightColors = LightColors(
    background = Color(0xFF000000),
    content = Color(0xFFFFFFFF),
    contentSecondary = Color(0xFFBBBBBB),
)

val LightLightColors = LightColors(
    background = Color(0xFFFFFFFF),
    content = Color(0xFF000000),
    contentSecondary = Color(0xFF666666),
)
