package app.candid.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.candid.theme.CandidTheme
import app.candid.theme.gridUnitsAsDp

@Composable
fun LightTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
) {
    Box(modifier = modifier.fillMaxWidth().hairlineBorder(CandidTheme.colors.contentSecondary)) {
        if (value.isEmpty()) {
            LightText(
                text = placeholder,
                variant = LightTextVariant.Paragraph,
                secondary = true,
                modifier = Modifier.padding(gridUnitsAsDp(1f)),
            )
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth().padding(gridUnitsAsDp(1f)),
            textStyle = CandidTheme.typography.paragraph.copy(color = CandidTheme.colors.content),
            cursorBrush = androidx.compose.ui.graphics.SolidColor(CandidTheme.colors.content),
        )
    }
}
