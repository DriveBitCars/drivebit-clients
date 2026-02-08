package my.drivebit.components

import androidx.compose.runtime.Composable
import my.drivebit.design.CSSColors
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
