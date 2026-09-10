package app.candid.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import app.candid.capture.CaptureSettings
import app.candid.notifications.ReminderOverlay
import app.candid.notifications.ReminderScheduler
import app.candid.notifications.ReminderStyle
import app.candid.notifications.ReminderStyleSettings
import app.candid.notifications.rememberOverlayPermissionRequester
import app.candid.theme.CandidTheme
import app.candid.theme.gridUnitsAsDp
import app.candid.ui.components.BarButton
import app.candid.ui.components.LightBottomBar
import app.candid.ui.components.LightText
import app.candid.ui.components.LightTextVariant
import app.candid.ui.components.LightTopBar
import app.candid.ui.components.hairlineBorder
import app.candid.ui.components.lightClickable
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun SettingsScreen(
    reminderScheduler: ReminderScheduler,
    reminderStyleSettings: ReminderStyleSettings,
    captureSettings: CaptureSettings,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val (initialStart, initialEnd) = remember { reminderScheduler.getWindow() }
    var startHour by remember { mutableIntStateOf(initialStart) }
    var endHour by remember { mutableIntStateOf(initialEnd) }
    var activeDays by remember { mutableStateOf(reminderScheduler.getActiveDays()) }
    var reminderStyle by remember { mutableStateOf(reminderStyleSettings.getStyle()) }
    var overlayGranted by remember { mutableStateOf(ReminderOverlay.canShow(context)) }
    val requestOverlayPermission = rememberOverlayPermissionRequester { granted -> overlayGranted = granted }
    var autoDualCapture by remember { mutableStateOf(captureSettings.isAutoDualCaptureEnabled()) }
    val formatter = DateTimeFormatter.ofPattern("h a")

    Column(Modifier.fillMaxSize()) {
        LightTopBar(left = BarButton(label = "Back", onClick = onBack), title = "Settings")

        Column(
            modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(gridUnitsAsDp(1f)),
        ) {
            LightText(
                "Reminder days",
                variant = LightTextVariant.Subheading,
            )
            LightText(
                "Candid only prompts you on the days you pick here.",
                variant = LightTextVariant.Paragraph,
                secondary = true,
                modifier = Modifier.padding(top = gridUnitsAsDp(0.25f), bottom = gridUnitsAsDp(1f)),
            )
            DayOfWeekPicker(
                activeDays = activeDays,
                onToggle = { day ->
                    val toggled = if (day in activeDays) activeDays - day else activeDays + day
                    if (toggled.isNotEmpty()) activeDays = toggled
                },
            )

            LightText(
                "Reminder window",
                variant = LightTextVariant.Subheading,
                modifier = Modifier.padding(top = gridUnitsAsDp(1.5f)),
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

            LightText(
                "Reminder style",
                variant = LightTextVariant.Subheading,
                modifier = Modifier.padding(top = gridUnitsAsDp(1.5f)),
            )
            LightText(
                "Gentle posts a normal notification. Overlay draws a card over whatever you're doing instead — closer to an alarm, so it's visible even on stock LightOS with no way to check notifications. If your phone has a lock set, Overlay shows behind it rather than bypassing it.",
                variant = LightTextVariant.Paragraph,
                secondary = true,
                modifier = Modifier.padding(top = gridUnitsAsDp(0.25f), bottom = gridUnitsAsDp(1f)),
            )
            ReminderStylePicker(
                style = reminderStyle,
                onChange = { reminderStyle = it },
            )
            if (reminderStyle == ReminderStyle.OVERLAY && !overlayGranted) {
                LightText(
                    "Needs the \"display over other apps\" permission, or reminders fall back to Gentle.",
                    variant = LightTextVariant.Superfine,
                    secondary = true,
                    modifier = Modifier.padding(top = gridUnitsAsDp(0.5f)),
                )
                LightText(
                    "Grant permission",
                    variant = LightTextVariant.Superfine,
                    underline = true,
                    modifier = Modifier
                        .padding(top = gridUnitsAsDp(0.25f))
                        .lightClickable { requestOverlayPermission() },
                )
            }

            LightText(
                "Capture mode",
                variant = LightTextVariant.Subheading,
                modifier = Modifier.padding(top = gridUnitsAsDp(1.5f)),
            )
            LightText(
                "Auto takes the rear and front photo with one tap. Manual lets you skip the front photo, so a selfie is never forced.",
                variant = LightTextVariant.Paragraph,
                secondary = true,
                modifier = Modifier.padding(top = gridUnitsAsDp(0.25f), bottom = gridUnitsAsDp(1f)),
            )
            CaptureModePicker(
                autoDualCapture = autoDualCapture,
                onChange = { autoDualCapture = it },
            )
        }

        LightBottomBar(
            items = listOf(
                BarButton(
                    label = "Save",
                    onClick = {
                        reminderScheduler.setWindow(startHour, endHour)
                        reminderScheduler.setActiveDays(activeDays)
                        reminderStyleSettings.setStyle(reminderStyle)
                        captureSettings.setAutoDualCaptureEnabled(autoDualCapture)
                        onBack()
                    },
                ),
            ),
        )
    }
}

private val dayOrder = listOf(
    DayOfWeek.SUNDAY,
    DayOfWeek.MONDAY,
    DayOfWeek.TUESDAY,
    DayOfWeek.WEDNESDAY,
    DayOfWeek.THURSDAY,
    DayOfWeek.FRIDAY,
    DayOfWeek.SATURDAY,
)

@Composable
private fun DayOfWeekPicker(
    activeDays: Set<DayOfWeek>,
    onToggle: (DayOfWeek) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().hairlineBorder(CandidTheme.colors.contentSecondary).padding(gridUnitsAsDp(0.5f)),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        dayOrder.forEach { day ->
            val selected = day in activeDays
            LightText(
                day.name.take(1),
                variant = LightTextVariant.Paragraph,
                secondary = !selected,
                underline = selected,
                modifier = Modifier.lightClickable { onToggle(day) },
            )
        }
    }
}

@Composable
private fun ReminderStylePicker(
    style: ReminderStyle,
    onChange: (ReminderStyle) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().hairlineBorder(CandidTheme.colors.contentSecondary).padding(gridUnitsAsDp(0.5f)),
        horizontalArrangement = Arrangement.spacedBy(gridUnitsAsDp(2f)),
    ) {
        LightText(
            "Gentle",
            variant = LightTextVariant.Paragraph,
            secondary = style != ReminderStyle.GENTLE,
            underline = style == ReminderStyle.GENTLE,
            modifier = Modifier.lightClickable { onChange(ReminderStyle.GENTLE) },
        )
        LightText(
            "Overlay",
            variant = LightTextVariant.Paragraph,
            secondary = style != ReminderStyle.OVERLAY,
            underline = style == ReminderStyle.OVERLAY,
            modifier = Modifier.lightClickable { onChange(ReminderStyle.OVERLAY) },
        )
    }
}

@Composable
private fun CaptureModePicker(
    autoDualCapture: Boolean,
    onChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().hairlineBorder(CandidTheme.colors.contentSecondary).padding(gridUnitsAsDp(0.5f)),
        horizontalArrangement = Arrangement.spacedBy(gridUnitsAsDp(2f)),
    ) {
        LightText(
            "Auto",
            variant = LightTextVariant.Paragraph,
            secondary = !autoDualCapture,
            underline = autoDualCapture,
            modifier = Modifier.lightClickable { onChange(true) },
        )
        LightText(
            "Manual",
            variant = LightTextVariant.Paragraph,
            secondary = autoDualCapture,
            underline = !autoDualCapture,
            modifier = Modifier.lightClickable { onChange(false) },
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
