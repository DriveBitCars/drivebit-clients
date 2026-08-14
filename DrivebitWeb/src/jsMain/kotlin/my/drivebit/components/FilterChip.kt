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

    val hasLighterBorder: Boolean
        get() = border == "gray"
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
            border = "gray",
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

data class FiltersResetChipStyle(
    val background: String,
    val border: String,
    val text: String,
    val hoverBackground: String,
) {
    val isFilled: Boolean
        get() = background == "blue" && text == "white"

    val isBrighterThanOutline: Boolean
        get() = isFilled
}

fun filtersResetChipStyle(): FiltersResetChipStyle =
    FiltersResetChipStyle(
        background = "blue",
        border = "blue",
        text = "white",
        hoverBackground = "blue-strong",
    )

@Composable
fun FiltersResetChip(onClick: () -> Unit) {
    val style = filtersResetChipStyle()
    Row(
        gap = 8.px,
        alignItems = AlignItems.Center,
        modifier = {
            padding(8.px, 14.px)
            backgroundColor(CSSColors.Blue)
            borderRadius(8.px)
            border(1.px, LineStyle.Solid, CSSColors.Blue)
            cursor("pointer")
            property("transition", "all 0.2s ease")
            property("box-shadow", "0 2px 6px rgba(41, 98, 255, 0.35)")
            property("flex-shrink", horizontalScrollRowStyle().childFlexShrink)
            property("white-space", "nowrap")
        },
        attrs = {
            onClick { onClick() }
            onMouseEnter {
                (it.currentTarget as? org.w3c.dom.HTMLElement)?.style?.setProperty(
                    "background-color",
                    CSSColors.BlueRedString,
                )
                (it.currentTarget as? org.w3c.dom.HTMLElement)?.style?.setProperty(
                    "border-color",
                    CSSColors.BlueRedString,
                )
            }
            onMouseLeave {
                (it.currentTarget as? org.w3c.dom.HTMLElement)?.style?.setProperty(
                    "background-color",
                    CSSColors.BlueString,
                )
                (it.currentTarget as? org.w3c.dom.HTMLElement)?.style?.setProperty(
                    "border-color",
                    CSSColors.BlueString,
                )
            }
        },
    ) {
        Span({
            style {
                fontSize(14.px)
                color(if (style.text == "white") CSSColors.White else CSSColors.Blue)
                fontWeight(CSSTypography.FontWeight.semibold)
            }
        }) {
            Text("Сбросить")
        }
    }
}
