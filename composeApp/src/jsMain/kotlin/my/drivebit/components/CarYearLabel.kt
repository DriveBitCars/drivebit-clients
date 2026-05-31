package my.drivebit.components

import androidx.compose.runtime.Composable
import my.drivebit.design.CSSColors
import org.jetbrains.compose.web.css.color
import org.jetbrains.compose.web.css.fontSize
import org.jetbrains.compose.web.css.fontWeight
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text

@Composable
fun CarYearLabel(year: Int) {
    if (year <= 0) return

    Div({
        style {
            fontSize(24.px)
            fontWeight("600")
            color(CSSColors.Gray600)
        }
    }) {
        Text("$year")
    }
}
