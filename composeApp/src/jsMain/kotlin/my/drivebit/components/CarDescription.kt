package my.drivebit.components

import androidx.compose.runtime.Composable
import my.drivebit.design.CSSColors
import org.jetbrains.compose.web.css.color
import org.jetbrains.compose.web.css.fontSize
import org.jetbrains.compose.web.css.fontWeight
import org.jetbrains.compose.web.css.lineHeight
import org.jetbrains.compose.web.css.marginTop
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text

@Composable
fun CarDescription(description: String?) {
    description
        ?.takeIf { it.isNotBlank() }
        ?.let { nonBlankDescription ->
            Div({
                style {
                    fontSize(16.px)
                    fontWeight("400")
                    color(CSSColors.Gray600)
                    marginTop(12.px)
                    lineHeight("1.5")
                }
            }) {
                Text(nonBlankDescription)
            }
        }
}
