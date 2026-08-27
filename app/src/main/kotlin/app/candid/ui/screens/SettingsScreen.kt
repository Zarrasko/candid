package app.candid.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import app.candid.notifications.ReminderScheduler
import app.candid.theme.CandidTheme
import app.candid.theme.gridUnitsAsDp
import app.candid.ui.components.BarButton
import app.candid.ui.components.LightBottomBar
import app.candid.ui.components.LightText
import app.candid.ui.components.LightTextVariant
import app.candid.ui.components.LightTopBar
import app.candid.ui.components.hairlineBorder
import app.candid.ui.components.lightClickable
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun SettingsScreen(
    reminderScheduler: ReminderScheduler,
    onBack: () -> Unit,
) {
    val (initialStart, initialEnd) = remember { reminderScheduler.getWindow() }
    var startHour by remember { mutableIntStateOf(initialStart) }
    var endHour by remember { mutableIntStateOf(initialEnd) }
    val formatter = DateTimeFormatter.ofPattern("h a")

    Column(Modifier.fillMaxSize()) {
        LightTopBar(left = BarButton(label = "Back", onClick = onBack), title = "Settings")

        Column(
            modifier = Modifier.weight(1f).fillMaxWidth().padding(gridUnitsAsDp(1f)),
        ) {
            LightText(
                "Reminder window",
                variant = LightTextVariant.Subheading,
            )
            LightText(
                "Candid prompts you once a day at a random time inside this window.",
                variant = LightTextVariant.Paragraph,
                secondary = true,
                modifier = Modifier.padding(top = gridUnitsAsDp(0.25f), bottom = gridUnitsAsDp(1f)),
            )

            HourStepper(
                label = "From",
                hour = startHour,
                onChange = { newStart ->
                    if (newStart < endHour) startHour = newStart
                },
                formatter = formatter,
            )
            HourStepper(
                label = "Until",
                hour = endHour,
                onChange = { newEnd ->
                    if (newEnd > startHour) endHour = newEnd
                },
                formatter = formatter,
                modifier = Modifier.padding(top = gridUnitsAsDp(0.5f)),
            )

            if (!reminderScheduler.hasExactAlarmPermission()) {
                LightText(
                    "Precise timing isn't available on this device — reminders may arrive a little early or late.",
                    variant = LightTextVariant.Superfine,
                    secondary = true,
                    modifier = Modifier.padding(top = gridUnitsAsDp(1f)),
                )
            }
        }

        LightBottomBar(
            items = listOf(
                BarButton(label = "Back", onClick = onBack),
                BarButton(
                    label = "Save",
                    onClick = {
                        reminderScheduler.setWindow(startHour, endHour)
                        onBack()
                    },
                ),
            ),
        )
    }
}

@Composable
private fun HourStepper(
    label: String,
    hour: Int,
    onChange: (Int) -> Unit,
    formatter: DateTimeFormatter,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().hairlineBorder(CandidTheme.colors.contentSecondary).padding(gridUnitsAsDp(0.5f)),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        LightText(label, variant = LightTextVariant.Paragraph)
        Row(horizontalArrangement = Arrangement.spacedBy(gridUnitsAsDp(1f))) {
            LightText(
                "-",
                variant = LightTextVariant.Paragraph,
                modifier = Modifier.lightClickable { onChange((hour - 1 + 24) % 24) },
            )
            LightText(LocalTime.of(hour, 0).format(formatter), variant = LightTextVariant.Paragraph)
            LightText(
                "+",
                variant = LightTextVariant.Paragraph,
                modifier = Modifier.lightClickable { onChange((hour + 1) % 24) },
            )
        }
    }
}
