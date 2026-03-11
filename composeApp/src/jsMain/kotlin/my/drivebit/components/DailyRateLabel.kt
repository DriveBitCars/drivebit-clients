package my.drivebit.components

import androidx.compose.runtime.Composable
import my.drivebit.design.CSSColors
import my.drivebit.design.CSSTypography
import my.drivebit.network.services.CarDetailResponse
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Table
import org.jetbrains.compose.web.dom.Tbody
import org.jetbrains.compose.web.dom.Td
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.dom.Tr

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

    val rates =
        listOfNotNull(
            if (dailyRate > 0) Pair("Сутки", dailyRate) else null,
            rate4?.let { Pair("4 дня", it) },
            rate7?.let { Pair("7 дней", it) },
            rate14?.let { Pair("14 дней", it) },
            rate21?.let { Pair("21 день", it) },
        )

    Div({
        style {
            marginTop(16.px)
            padding(24.px)
            property("background-color", CSSColors.WhiteString)
            property("border-radius", "12px")
            property("box-shadow", "0 4px 12px rgba(0, 0, 0, 0.1)")
        }
    }) {
        Table({
            style {
                property("width", "100%")
                property("border-collapse", "collapse")
                property("border-spacing", "0")
                fontSize(CSSTypography.FontSize.lg)
                fontWeight(CSSTypography.FontWeight.medium)
                color(CSSColors.Black)
            }
        }) {
            Tbody {
                rates.forEach { (label, rate) ->
                    Tr {
                        Td({
                            style {
                                padding(12.px, 16.px)
                                // property("border-bottom", "1px solid ${CSSColors.Gray300String}")
                            }
                        }) {
                            Text(label)
                        }
                        Td({
                            style {
                                padding(12.px, 16.px)
                                property("text-align", "right")
                                //   property("border-bottom", "1px solid ${CSSColors.Gray300String}")
                            }
                        }) {
                            Text("от ${rate.toInt()} ₽ / сутки")
                        }
                    }
                }
            }
        }
    }
}
