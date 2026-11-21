package my.drivebit.components

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.css.height
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.Img

@Composable
@Suppress("FunctionName")
fun MenuUserButton(onClick: () -> Unit) {
    UniversalButton(
        isSelected = false,
        onClick = {
            onClick()
        },
    ) {
        Img(
            src = "/images/chrome/icon-burger.svg",
            alt = "Menu",
            attrs = {
                style {
                    width(32.px)
                    height(32.px)
                }
            },
        )
    }
}
