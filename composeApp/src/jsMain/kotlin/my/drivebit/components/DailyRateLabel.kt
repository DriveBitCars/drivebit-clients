package my.drivebit.components

import androidx.compose.runtime.Composable
import my.drivebit.design.CSSColors
import org.jetbrains.compose.web.css.color
import org.jetbrains.compose.web.css.fontSize
import org.jetbrains.compose.web.css.fontWeight
import org.jetbrains.compose.web.css.marginTop
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text

@Composable
fun DailyRateLabel(dailyRate: Double) {
    if (dailyRate <= 0) return

    Div({
        style {
            fontSize(24.px)
            fontWeight("600")
            color(CSSColors.Black)
            marginTop(16.px)
        }
    }) {
        Text("от ${dailyRate.toInt()} ₽ / сутки")
    }
}
