package my.drivebit.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.browser.document
import my.drivebit.design.CSSColors
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Input

private var dualRangeSliderStylesInjected = false

private fun ensureDualRangeSliderStyles() {
    if (dualRangeSliderStylesInjected) return
    dualRangeSliderStylesInjected = true
    val blue = CSSColors.BlueString
    val style = document.createElement("style")
    style.id = "dual-range-slider-styles"
    style.innerHTML =
        """
        .dual-range-slider {
            position: absolute;
            left: 0;
            top: -4px;
            width: 100%;
            height: 20px;
            margin: 0;
            pointer-events: none;
            -webkit-appearance: none;
            appearance: none;
            background: transparent;
        }
        .dual-range-slider::-webkit-slider-runnable-track {
            background: transparent;
            border: none;
            height: 6px;
        }
        .dual-range-slider::-webkit-slider-thumb {
            -webkit-appearance: none;
            appearance: none;
            pointer-events: auto;
            width: 18px;
            height: 18px;
            border-radius: 50%;
            background: #fff;
            border: 2px solid $blue;
            margin-top: -6px;
            cursor: grab;
        }
        .dual-range-slider::-moz-range-track {
            background: transparent;
            border: none;
            height: 6px;
        }
        .dual-range-slider::-moz-range-thumb {
            pointer-events: auto;
            width: 18px;
            height: 18px;
            border-radius: 50%;
            background: #fff;
            border: 2px solid $blue;
            cursor: grab;
        }
        .dual-range-slider--min { z-index: 3; }
        .dual-range-slider--max { z-index: 4; }
        .dual-range-slider--active { z-index: 5 !important; }
        """.trimIndent()
    document.head?.appendChild(style)
}

private enum class ActiveRangeThumb {
    Min,
    Max,
}

@Composable
fun PriceRangeSlider(
    minPrice: MutableState<Int>,
    maxPrice: MutableState<Int>,
    onMinValueChange: (String) -> Unit,
    onMaxValueChange: (String) -> Unit,
    sliderMin: Int = 0,
    sliderMax: Int = 50000,
) {
    ensureDualRangeSliderStyles()

    val minValue by minPrice
    val maxValue by maxPrice
    var activeThumb by remember { mutableStateOf(ActiveRangeThumb.Max) }
    val range = (sliderMax - sliderMin).coerceAtLeast(1)
    val minPercent = ((minValue - sliderMin).toDouble() / range * 100).coerceIn(0.0, 100.0)
    val maxPercent = ((maxValue - sliderMin).toDouble() / range * 100).coerceIn(0.0, 100.0)

    Div({
        style {
            position(Position.Relative)
            height(20.px)
            width(100.percent)
            marginTop(8.px)
            marginBottom(4.px)
        }
        onMouseDown { event ->
            val target = event.target as? org.w3c.dom.Element ?: return@onMouseDown
            if (target !is org.w3c.dom.HTMLInputElement) {
                val rect = (event.currentTarget as org.w3c.dom.HTMLElement).getBoundingClientRect()
                val clickPercent = ((event.clientX - rect.left) / rect.width) * 100.0
                val midThumb = (minPercent + maxPercent) / 2.0
                activeThumb =
                    if (clickPercent <= midThumb) {
                        ActiveRangeThumb.Min
                    } else {
                        ActiveRangeThumb.Max
                    }
            }
        }
    }) {
        Div({
            style {
                position(Position.Absolute)
                left(0.px)
                top(6.px)
                property("width", "100%")
                height(6.px)
                backgroundColor(CSSColors.Gray300)
                borderRadius(3.px)
            }
        })

        Div({
            style {
                position(Position.Absolute)
                property("left", "$minPercent%")
                top(6.px)
                property("width", "${maxPercent - minPercent}%")
                height(6.px)
                backgroundColor(CSSColors.Blue)
                borderRadius(3.px)
                property("pointer-events", "none")
            }
        })

        Input(
            type = InputType.Range,
            attrs = {
                attr("min", sliderMin.toString())
                attr("max", sliderMax.toString())
                value(minValue.toString())
                if (activeThumb == ActiveRangeThumb.Min) {
                    classes(
                        "dual-range-slider",
                        "dual-range-slider--min",
                        "dual-range-slider--active",
                    )
                } else {
                    classes("dual-range-slider", "dual-range-slider--min")
                }
                onInput { event ->
                    val newValue =
                        (event.target as org.w3c.dom.HTMLInputElement).value.toIntOrNull() ?: sliderMin
                    if (newValue <= maxValue) {
                        minPrice.value = newValue
                        onMinValueChange("$newValue")
                    }
                }
                onMouseDown {
                    activeThumb = ActiveRangeThumb.Min
                }
                onFocus {
                    activeThumb = ActiveRangeThumb.Min
                }
            },
        )

        Input(
            type = InputType.Range,
            attrs = {
                attr("min", sliderMin.toString())
                attr("max", sliderMax.toString())
                value(maxValue.toString())
                if (activeThumb == ActiveRangeThumb.Max) {
                    classes(
                        "dual-range-slider",
                        "dual-range-slider--max",
                        "dual-range-slider--active",
                    )
                } else {
                    classes("dual-range-slider", "dual-range-slider--max")
                }
                onInput { event ->
                    val newValue =
                        (event.target as org.w3c.dom.HTMLInputElement).value.toIntOrNull() ?: sliderMax
                    if (newValue >= minValue) {
                        maxPrice.value = newValue
                        onMaxValueChange("$newValue+")
                    }
                }
                onMouseDown {
                    activeThumb = ActiveRangeThumb.Max
                }
                onFocus {
                    activeThumb = ActiveRangeThumb.Max
                }
            },
        )
    }
}
