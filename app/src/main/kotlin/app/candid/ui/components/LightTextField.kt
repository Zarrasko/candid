package app.candid.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import app.candid.theme.CandidTheme
import app.candid.theme.gridUnitsAsDp

@Composable
fun LightTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

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
            singleLine = true,
            // Not every keyboard on LP3 shows a dismiss arrow, and LightOS disables the
            // edge-swipe gesture that would normally close it — the Done/Enter key is the
            // one dismissal path guaranteed to exist regardless of which IME is active.
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                },
            ),
        )
    }
}
