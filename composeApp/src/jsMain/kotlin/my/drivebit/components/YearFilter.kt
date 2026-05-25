package my.drivebit.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import my.drivebit.design.CSSColors
import my.drivebit.design.CSSTypography
import my.drivebit.design.applyTypography
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text

const val YEAR_FILTER_MIN = 1990

fun currentCalendarYear(): Int = Clock.System.todayIn(TimeZone.currentSystemDefault()).year

@Composable
fun YearFilter(
    minYear: MutableState<Int>,
    maxYear: MutableState<Int>,
    onReset: () -> Unit,
    onViewResults: () -> Unit,
) {
    val sliderMax = currentCalendarYear()
    val minValue by minYear
    val maxValue by maxYear
    var minInputValue by remember { mutableStateOf("$minValue") }
    var maxInputValue by remember { mutableStateOf("$maxValue") }

    minInputValue = "$minValue"
    maxInputValue = "$maxValue"

    Column(
        gap = 16.px,
        modifier = {
            padding(24.px)
            backgroundColor(CSSColors.White)
            borderRadius(12.px)
            property("box-shadow", "0 4px 12px rgba(0, 0, 0, 0.1)")
            width(400.px)
            property("max-width", "90vw")
        },
    ) {
        Span({
            style {
                applyTypography(CSSTypography.Styles.body)
                fontSize(CSSTypography.FontSize.base)
                fontWeight(CSSTypography.FontWeight.semibold)
                color(CSSColors.Black)
                textAlign("center")
                width(100.percent)
            }
        }) {
            Text("Год выпуска")
        }

        Row(
            gap = 12.px,
            alignItems = AlignItems.FlexStart,
        ) {
            PriceInputField(
                label = "От",
                value = minInputValue,
                onValueChange = { newValue ->
                    minYear.value = newValue
                    minInputValue = "$newValue"
                },
                minValue = YEAR_FILTER_MIN,
                maxValue = maxValue,
            )

            PriceInputField(
                label = "До",
                value = maxInputValue,
                onValueChange = { newValue ->
                    maxYear.value = newValue
                    maxInputValue = "$newValue"
                },
                minValue = minValue,
                maxValue = sliderMax,
            )
        }

        PriceRangeSlider(
            minPrice = minYear,
            maxPrice = maxYear,
            onMinValueChange = { minInputValue = it },
            onMaxValueChange = { maxInputValue = it },
            sliderMin = YEAR_FILTER_MIN,
            sliderMax = sliderMax,
        )

        Row(
            gap = 12.px,
            alignItems = AlignItems.Center,
        ) {
            Button({
                onClick {
                    minYear.value = YEAR_FILTER_MIN
                    maxYear.value = sliderMax
                    minInputValue = "$YEAR_FILTER_MIN"
                    maxInputValue = "$sliderMax"
                    onReset()
                }
                style {
                    flex(1)
                    padding(12.px, 24.px)
                    borderRadius(8.px)
                    backgroundColor(CSSColors.White)
                    border(1.px, LineStyle.Solid, CSSColors.Gray300)
                    color(CSSColors.Gray600)
                    cursor("pointer")
                    applyTypography(CSSTypography.Styles.button)
                    fontSize(CSSTypography.FontSize.base)
                    fontWeight(CSSTypography.FontWeight.medium)
                    property("transition", "all 0.2s ease")
                }
                onMouseEnter {
                    (it.target as org.w3c.dom.HTMLButtonElement).style.setProperty(
                        "background-color",
                        CSSColors.Gray300String,
                    )
                }
                onMouseLeave {
                    (it.target as org.w3c.dom.HTMLButtonElement).style.setProperty(
                        "background-color",
                        CSSColors.WhiteString,
                    )
                }
            }) {
                Text("Сбросить")
            }

            Button({
                onClick { onViewResults() }
                style {
                    flex(1)
                    padding(12.px, 24.px)
                    borderRadius(8.px)
                    backgroundColor(CSSColors.Blue)
                    border(0.px)
                    color(CSSColors.White)
                    cursor("pointer")
                    applyTypography(CSSTypography.Styles.button)
                    fontSize(CSSTypography.FontSize.base)
                    fontWeight(CSSTypography.FontWeight.semibold)
                    property("transition", "background-color 0.2s ease")
                }
                onMouseEnter {
                    (it.target as org.w3c.dom.HTMLButtonElement).style.setProperty(
                        "background-color",
                        CSSColors.BlueRedString,
                    )
                }
                onMouseLeave {
                    (it.target as org.w3c.dom.HTMLButtonElement).style.setProperty(
                        "background-color",
                        CSSColors.BlueString,
                    )
                }
            }) {
                Text("Показать")
            }
        }
    }
}
