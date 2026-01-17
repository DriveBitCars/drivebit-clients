package my.drivebit.components

import androidx.compose.runtime.Composable
import my.drivebit.design.CSSColors
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.css.rgb
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Img
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text

@Composable
fun CarSpecChip(
    iconPath: String,
    text: String,
) {
    Div({
        style {
            display(DisplayStyle.Flex)
            alignItems(AlignItems.Center)
            gap(8.px)
            padding(8.px, 12.px)
            backgroundColor(rgb(245, 245, 247))
            borderRadius(8.px)
            border(1.px, LineStyle.Solid, CSSColors.Gray300)
        }
    }) {
        Img(
            src = iconPath,
            alt = text,
            attrs = {
                style {
                    width(20.px)
                    height(20.px)
                    property("object-fit", "contain")
                }
            },
        )
        Span({
            style {
                fontSize(14.px)
                color(CSSColors.Black)
                fontWeight("500")
            }
        }) {
            Text(text)
        }
    }
}
