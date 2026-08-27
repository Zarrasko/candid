package app.candid.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Light's layout is defined on a 27x31 grid rather than fixed dp values, so it stays
 * proportionally consistent across screen sizes. Typography is scaled against a 600px
 * design-height baseline the same way.
 */
private const val GRID_COLUMNS = 27
private const val GRID_ROWS = 31
private const val DESIGN_HEIGHT_PX = 600f

@Composable
fun gridUnitsAsDp(units: Float): Dp {
    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    return (screenWidthDp / GRID_COLUMNS * units).dp
}

@Composable
fun verticalGridUnitsAsDp(units: Float): Dp {
    val screenHeightDp = LocalConfiguration.current.screenHeightDp
    return (screenHeightDp / GRID_ROWS * units).dp
}

@Composable
fun Float.designVerticalPxToSp(): TextUnit {
    val screenHeightDp = LocalConfiguration.current.screenHeightDp
    return (this * screenHeightDp / DESIGN_HEIGHT_PX).sp
}

@Composable
fun Float.designVerticalPxToDp(): Dp {
    val screenHeightDp = LocalConfiguration.current.screenHeightDp
    return (this * screenHeightDp / DESIGN_HEIGHT_PX).dp
}
