package my.drivebit.components

import androidx.compose.runtime.Composable
import my.drivebit.design.CSSColors
import org.jetbrains.compose.web.css.color
import org.jetbrains.compose.web.css.fontSize
import org.jetbrains.compose.web.css.fontWeight
import org.jetbrains.compose.web.css.lineHeight
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text

@Composable
@Suppress("FunctionName")
fun CarDetailAddress(address: String?) {
    address?.takeIf { it.isNotBlank() }?.let { text ->
        Div({
            classes("db-car-section", "db-car-address")
        }) {
            Span({
                style {
                    fontSize(13.px)
                    fontWeight("600")
                    color(CSSColors.Gray4)
                    property("text-transform", "uppercase")
                    property("letter-spacing", "0.04em")
                    property("display", "block")
                    property("margin-bottom", "6px")
                    property("width", "100%")
                }
            }) {
                Text("Адрес выдачи")
            }
            Div({
                style {
                    fontSize(15.px)
                    color(CSSColors.Black)
                    lineHeight("1.5")
                    property("width", "100%")
                }
            }) {
                Text(text)
            }
        }
    }
}
