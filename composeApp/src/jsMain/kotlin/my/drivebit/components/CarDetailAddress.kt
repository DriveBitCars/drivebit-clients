package my.drivebit.components

import androidx.compose.runtime.Composable
import my.drivebit.design.CSSColors
import org.jetbrains.compose.web.css.color
import org.jetbrains.compose.web.css.fontSize
import org.jetbrains.compose.web.css.lineHeight
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text

@Composable
@Suppress("FunctionName")
fun CarDetailAddress(address: String?) {
    address?.takeIf { it.isNotBlank() }?.let { text ->
        Div({
            style {
                fontSize(15.px)
                color(CSSColors.Gray600)
                lineHeight("1.5")
            }
        }) {
            Text(text)
        }
    }
}
