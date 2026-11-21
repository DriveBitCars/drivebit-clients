package my.drivebit.components

import androidx.compose.runtime.Composable
import my.drivebit.design.CSSColors
import my.drivebit.design.CSSTypography
import my.drivebit.network.services.CarDetailResponse
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text

@Composable
fun DailyRateLabel(dailyRate: Int) {
    if (dailyRate <= 0) return

    Div({
        style {
            fontSize(24.px)
            fontWeight("700")
            color(CSSColors.Blue)
            marginTop(8.px)
            property("letter-spacing", "-0.02em")
        }
    }) {
        Text("от $dailyRate ₽ / сутки")
    }
}

@Composable
fun CarRatesSection(car: CarDetailResponse) {
    val dailyRate = car.resolvedDailyRate()
    val rate4 = car.dailyRate4Days?.takeIf { it > 0 }?.toInt()
    val rate7 = car.dailyRate7Days?.takeIf { it > 0 }?.toInt()
    val rate14 = car.dailyRate14Days?.takeIf { it > 0 }?.toInt()
    val rate21 = car.dailyRate21Days?.takeIf { it > 0 }?.toInt()
    val hasAnyRate = dailyRate > 0 || rate4 != null || rate7 != null || rate14 != null || rate21 != null
    if (!hasAnyRate) return

    val rates =
        listOfNotNull(
            if (dailyRate > 0) Pair("1 сутки", dailyRate) else null,
            rate4?.let { Pair("4 дня", it) },
            rate7?.let { Pair("7 дней", it) },
            rate14?.let { Pair("14 дней", it) },
            rate21?.let { Pair("21 день", it) },
        )

    Div({
        classes("db-car-section")
    }) {
        Div({
            classes("db-car-rates")
        }) {
            rates.forEach { (label, rate) ->
                Div({
                    classes("db-car-rate")
                }) {
                    Span({
                        classes("db-car-rate__label")
                    }) {
                        Text(label)
                    }
                    Span({
                        classes("db-car-rate__value")
                    }) {
                        Text("от $rate ₽ / сутки")
                    }
                }
            }
        }
    }
}

@Composable
fun CarInsuranceSection(car: CarDetailResponse) {
    val text = car.resolvedInsuranceDisplay() ?: return

    Div({
        classes("db-car-section")
    }) {
        Div({
            classes("db-car-section__title")
        }) {
            Text("Страховка")
        }
        Div({
            style {
                fontSize(CSSTypography.FontSize.lg)
                fontWeight(CSSTypography.FontWeight.normal)
                color(CSSColors.Gray4)
                property("line-height", "1.5")
            }
        }) {
            Text(text)
        }
    }
}

@Composable
fun CarTravelDestinationsSection(car: CarDetailResponse) {
    val text = car.resolvedTravelDestinationsDisplay() ?: return

    Div({
        classes("db-car-section")
    }) {
        Div({
            classes("db-car-section__title")
        }) {
            Text("Направления путешествий")
        }
        Div({
            style {
                fontSize(CSSTypography.FontSize.lg)
                fontWeight(CSSTypography.FontWeight.normal)
                color(CSSColors.Gray4)
                property("line-height", "1.5")
            }
        }) {
            Text(text)
        }
    }
}

@Composable
fun CarDepositSection(car: CarDetailResponse) {
    val deposit = car.deposit?.takeIf { it > 0 }?.toInt() ?: return

    Div({
        classes("db-car-section")
    }) {
        Div({
            style {
                display(DisplayStyle.Flex)
                property("justify-content", "space-between")
                property("align-items", "center")
                gap(12.px)
            }
        }) {
            Div({
                style {
                    fontSize(CSSTypography.FontSize.lg)
                    fontWeight(CSSTypography.FontWeight.medium)
                    color(CSSColors.Black)
                }
            }) {
                Text("Залог")
            }
            Div({
                style {
                    fontSize(CSSTypography.FontSize.lg)
                    fontWeight(CSSTypography.FontWeight.medium)
                    color(CSSColors.Black)
                }
            }) {
                Text("$deposit ₽")
            }
        }
    }
}
