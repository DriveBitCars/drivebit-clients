package my.drivebit.components

import androidx.compose.runtime.Composable
import my.drivebit.components.Column
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
fun CarDescription(description: String?) {
    description
        ?.takeIf { it.isNotBlank() }
        ?.let { nonBlankDescription ->
            Column(
                gap = 12.px,
                attrs = { classes("db-car-section") },
            ) {
                Span({
                    classes("db-car-section__title")
                    style {
                        fontSize(16.px)
                        fontWeight("600")
                        color(CSSColors.Black)
                    }
                }) {
                    Text("Описание")
                }

                Div({
                    style {
                        fontSize(15.px)
                        fontWeight("400")
                        color(CSSColors.Gray4)
                        lineHeight("1.6")
                    }
                }) {
                    Text(nonBlankDescription)
                }
            }
        }
}
