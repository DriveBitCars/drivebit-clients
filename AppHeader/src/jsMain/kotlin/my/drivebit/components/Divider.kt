package my.drivebit.components

import androidx.compose.runtime.Composable
import my.drivebit.design.CSSColors
import org.jetbrains.compose.web.css.CSSColorValue
import org.jetbrains.compose.web.css.CSSSizeValue
import org.jetbrains.compose.web.css.backgroundColor
import org.jetbrains.compose.web.css.height
import org.jetbrains.compose.web.css.marginTop
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.Div

@Composable
fun Divider(
    color: CSSColorValue = CSSColors.Gray300,
    thickness: CSSSizeValue<*>? = null,
    marginTop: CSSSizeValue<*>? = null,
) {
    val lineThickness = thickness ?: 1.px
    Div({
        style {
            width(100.percent)
            height(lineThickness)
            backgroundColor(color)
            marginTop?.let { marginTop(it) }
        }
    })
}
