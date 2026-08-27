package app.candid.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember

val LocalLightColors = compositionLocalOf { DarkLightColors }
val LocalLightTypography = compositionLocalOf<LightTypography> { error("LightTheme not applied") }

object CandidTheme {
    val colors: LightColors
        @Composable get() = LocalLightColors.current

    val typography: LightTypography
        @Composable get() = LocalLightTypography.current
}

@Composable
fun CandidTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colors = remember(darkTheme) { if (darkTheme) DarkLightColors else LightLightColors }
    val typography = buildLightTypography()

    CompositionLocalProvider(
        LocalLightColors provides colors,
        LocalLightTypography provides typography,
        content = content,
    )
}
