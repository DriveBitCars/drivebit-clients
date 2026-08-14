package my.drivebit.components

import androidx.compose.runtime.Composable
import my.drivebit.design.CSSColors
import my.drivebit.design.CSSTypography
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text

data class FilterChipStyle(
    val background: String,
    val border: String,
    val text: String,
    val arrow: String,
) {
    val usesNeutralChrome: Boolean
        get() = background == "white" && border == "gray" && text == "black"

    val isFilled: Boolean
        get() = background == "blue" && text == "white"
}

fun filterChipStyle(isSelected: Boolean): FilterChipStyle =
    if (isSelected) {
        FilterChipStyle(
            background = "blue",
            border = "blue",
            text = "white",
            arrow = "white",
        )
    } else {
        FilterChipStyle(
            background = "white",
            border = "blue",
            text = "blue",
            arrow = "blue",
        )
    }

@Composable
fun FilterChip(
    name: String,
    onClick: () -> Unit,
    isSelected: Boolean = false,
    selectedText: String? = null,
) {
    val style = filterChipStyle(isSelected)
    Row(
        gap = 8.px,
        alignItems = AlignItems.Center,
        modifier = {
            padding(8.px, 12.px)
            backgroundColor(if (style.background == "blue") CSSColors.Blue else CSSColors.White)
            borderRadius(8.px)
            border(
                1.px,
                LineStyle.Solid,
                if (style.border == "blue") CSSColors.Blue else CSSColors.Gray300,
            )
            cursor("pointer")
            property("transition", "all 0.2s ease")
            property("flex-shrink", horizontalScrollRowStyle().childFlexShrink)
            property("white-space", "nowrap")
        },
        attrs = {
            onClick { onClick() }
        },
    ) {
        Span({
            style {
                fontSize(14.px)
                color(
                    when (style.text) {
                        "blue" -> CSSColors.Blue
                        "white" -> CSSColors.White
                        else -> CSSColors.Black
                    },
                )
                fontWeight("400")
            }
        }) {
            Text(selectedText ?: name)
        }
        FilterChipArrow(color = style.arrow)
    }
}

@Composable
private fun FilterChipArrow(color: String) {
    Div({
        attr("aria-hidden", "true")
        style {
            width(16.px)
            height(10.px)
            backgroundColor(
                when (color) {
                    "blue" -> CSSColors.Blue
                    "white" -> CSSColors.White
                    else -> CSSColors.Black
                },
            )
            property("mask-image", "url(/images/arrow-bottom.svg)")
            property("mask-size", "contain")
            property("mask-repeat", "no-repeat")
            property("mask-position", "center")
            property("-webkit-mask-image", "url(/images/arrow-bottom.svg)")
            property("-webkit-mask-size", "contain")
            property("-webkit-mask-repeat", "no-repeat")
            property("-webkit-mask-position", "center")
        }
    })
}

private const val RESET_CHIP_BG = "rgba(41, 98, 255, 0.12)"
private const val RESET_CHIP_BG_HOVER = "rgba(41, 98, 255, 0.22)"

@Composable
fun FiltersResetChip(onClick: () -> Unit) {
    Row(
        gap = 8.px,
        alignItems = AlignItems.Center,
        modifier = {
            padding(8.px, 14.px)
            property("background-color", RESET_CHIP_BG)
            borderRadius(8.px)
            border(1.px, LineStyle.Solid, CSSColors.Blue)
            cursor("pointer")
            property("transition", "all 0.2s ease")
            property("box-shadow", "0 1px 2px rgba(41, 98, 255, 0.15)")
            property("flex-shrink", horizontalScrollRowStyle().childFlexShrink)
            property("white-space", "nowrap")
        },
        attrs = {
            onClick { onClick() }
            onMouseEnter {
                (it.target as? org.w3c.dom.HTMLElement)?.style?.setProperty(
                    "background-color",
                    RESET_CHIP_BG_HOVER,
                )
            }
            onMouseLeave {
                (it.target as? org.w3c.dom.HTMLElement)?.style?.setProperty(
                    "background-color",
                    RESET_CHIP_BG,
                )
            }
        },
    ) {
        Span({
            style {
                fontSize(14.px)
                color(CSSColors.Blue)
                fontWeight(CSSTypography.FontWeight.semibold)
            }
        }) {
            Text("Сбросить")
        }
    }
}
