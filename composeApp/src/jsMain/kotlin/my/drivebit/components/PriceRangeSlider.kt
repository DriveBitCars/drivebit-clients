package my.drivebit.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import my.drivebit.design.CSSColors
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Input

@Composable
fun PriceRangeSlider(
    minPrice: MutableState<Int>,
    maxPrice: MutableState<Int>,
    onMinValueChange: (String) -> Unit,
    onMaxValueChange: (String) -> Unit,
    sliderMax: Int = 1000,
) {
    val minValue by minPrice
    val maxValue by maxPrice
    val minPercent = (minValue.toDouble() / sliderMax * 100).coerceIn(0.0, 100.0)
    val maxPercent = (maxValue.toDouble() / sliderMax * 100).coerceIn(0.0, 100.0)

    Div({
        style {
            position(Position.Relative)
            height(6.px)
            width(100.percent)
            backgroundColor(CSSColors.Gray300)
            borderRadius(3.px)
            marginTop(8.px)
            marginBottom(4.px)
        }
    }) {
        Div({
            style {
                position(Position.Absolute)
                property("left", "$minPercent%")
                top(0.px)
                property("width", "${maxPercent - minPercent}%")
                height(100.percent)
                backgroundColor(CSSColors.Blue)
                borderRadius(3.px)
            }
        })

        Input(
            type = InputType.Range,
            attrs = {
                attr("min", "0")
                attr("max", sliderMax.toString())
                value(minValue.toString())
                onInput { event ->
                    val newValue = (event.target as org.w3c.dom.HTMLInputElement).value.toIntOrNull() ?: 0
                    if (newValue <= maxValue) {
                        minPrice.value = newValue
                        onMinValueChange("$newValue")
                    }
                }
                style {
                    position(Position.Absolute)
                    left(0.px)
                    top(-4.px)
                    width(100.percent)
                    height(14.px)
                    property("opacity", "0")
                    property("cursor", "pointer")
                    property("z-index", "3")
                    property("margin", "0")
                }
            },
        )

        Input(
            type = InputType.Range,
            attrs = {
                attr("min", "0")
                attr("max", sliderMax.toString())
                value(maxValue.toString())
                onInput { event ->
                    val newValue = (event.target as org.w3c.dom.HTMLInputElement).value.toIntOrNull() ?: 600
                    if (newValue >= minValue) {
                        maxPrice.value = newValue
                        onMaxValueChange("$newValue+")
                    }
                }
                style {
                    position(Position.Absolute)
                    left(0.px)
                    top(-4.px)
                    width(100.percent)
                    height(14.px)
                    property("opacity", "0")
                    property("cursor", "pointer")
                    property("z-index", "4")
                    property("margin", "0")
                }
            },
        )
    }
}
