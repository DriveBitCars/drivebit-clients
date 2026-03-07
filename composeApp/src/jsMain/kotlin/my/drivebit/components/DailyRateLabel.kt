package my.drivebit.components

import androidx.compose.runtime.Composable
import my.drivebit.design.CSSColors
import my.drivebit.network.services.CarDetailResponse
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

@Composable
fun CarRatesSection(car: CarDetailResponse) {
    val dailyRate = car.resolvedDailyRate()
    val rate4 = car.dailyRate4Days?.takeIf { it > 0 }
    val rate7 = car.dailyRate7Days?.takeIf { it > 0 }
    val rate14 = car.dailyRate14Days?.takeIf { it > 0 }
    val rate21 = car.dailyRate21Days?.takeIf { it > 0 }
    val hasAnyRate = dailyRate > 0 || rate4 != null || rate7 != null || rate14 != null || rate21 != null
    if (!hasAnyRate) return

    Column(gap = 4.px, modifier = { marginTop(16.px) }) {
        val rateStyle: org.jetbrains.compose.web.css.StyleScope.() -> Unit = {
            fontSize(32.px)
            fontWeight("500")
            color(CSSColors.Black)
        }
        if (dailyRate > 0) {
            Div({ style(rateStyle) }) {
                Text("от ${dailyRate.toInt()} ₽ / сутки")
            }
        }
        listOf(
            Pair("4 дня", rate4),
            Pair("7 дней", rate7),
            Pair("14 дней", rate14),
            Pair("21 день", rate21),
        ).forEach { (label, rate) ->
            rate?.let {
                Div({ style(rateStyle) }) {
                    Text("$label: от ${it.toInt()} ₽ / сутки")
                }
            }
        }
    }
}
