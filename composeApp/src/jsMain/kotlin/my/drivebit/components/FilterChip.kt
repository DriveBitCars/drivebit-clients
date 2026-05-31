package my.drivebit.components

import androidx.compose.runtime.Composable
import my.drivebit.design.CSSColors
import my.drivebit.design.CSSTypography
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Img
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text

@Composable
fun FilterChip(
    name: String,
    onClick: () -> Unit,
    isSelected: Boolean = false,
    selectedText: String? = null,
) {
    Row(
        gap = 8.px,
        alignItems = AlignItems.Center,
        modifier = {
            padding(8.px, 12.px)
            backgroundColor(if (isSelected) CSSColors.Black else CSSColors.White)
            borderRadius(8.px)
            border(1.px, LineStyle.Solid, CSSColors.Gray300)
            cursor("pointer")
            property("transition", "all 0.2s ease")
        },
        attrs = {
            onClick { onClick() }
        },
    ) {
        Span({
            style {
                fontSize(14.px)
                color(if (isSelected) CSSColors.White else CSSColors.Black)
                fontWeight("400")
            }
        }) {
            Text(selectedText ?: name)
        }
        Img(
            src = "/images/arrow-bottom.svg",
            alt = "arrow",
            attrs = {
                style {
                    width(16.px)
                    height(10.px)
                    // paddingTop(8.px)
                }
            },
        )
    }
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
