package my.drivebit.components

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Img

@Composable
fun UserAvatar(
    src: String,
    size: CSSLengthOrPercentageValue = 200.px,
    alt: String = "User",
) {
    Img(
        src = src,
        alt = alt,
        attrs = {
            style {
                width(size)
                height(size)
                borderRadius(50.percent)
                property("object-fit", "cover")
                property("object-position", "top")
            }
        },
    )
}
