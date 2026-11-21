package my.drivebit.components

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.css.AlignItems
import org.jetbrains.compose.web.css.height
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.Img
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text

@Composable
fun CarSpecChip(
    iconPath: String,
    text: String,
) {
    Row(
        gap = 8.px,
        alignItems = AlignItems.Center,
        attrs = { classes("db-car-chip") },
    ) {
        Img(
            src = iconPath,
            alt = text,
            attrs = {
                style {
                    width(18.px)
                    height(18.px)
                    property("object-fit", "contain")
                }
            },
        )
        Span {
            Text(text)
        }
    }
}
