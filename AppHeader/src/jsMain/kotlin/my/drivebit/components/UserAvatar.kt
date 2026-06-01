package my.drivebit.components

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Img

@Composable
fun UserAvatar(
    src: String,
    size: CSSLengthOrPercentageValue? = null,
    alt: String = "User",
) {
    val avatarSize = size ?: 200.px
    Img(
        src = src,
        alt = alt,
        attrs = {
            style {
                width(avatarSize)
                height(avatarSize)
                borderRadius(50.percent)
                property("object-fit", "cover")
                property("object-position", "top")
            }
        },
    )
}
