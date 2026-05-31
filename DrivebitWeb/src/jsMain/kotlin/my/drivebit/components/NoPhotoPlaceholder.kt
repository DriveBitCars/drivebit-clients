package my.drivebit.components

import androidx.compose.runtime.Composable
import my.drivebit.design.CSSColors
import org.jetbrains.compose.web.css.AlignItems
import org.jetbrains.compose.web.css.CSSSizeValue
import org.jetbrains.compose.web.css.JustifyContent
import org.jetbrains.compose.web.css.alignItems
import org.jetbrains.compose.web.css.backgroundColor
import org.jetbrains.compose.web.css.borderRadius
import org.jetbrains.compose.web.css.height
import org.jetbrains.compose.web.css.justifyContent
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.Text

@Composable
fun NoPhotoPlaceholder(height: CSSSizeValue<*> = 400.px) {
    Row(
        alignItems = AlignItems.Center,
        justifyContent = JustifyContent.Center,
        modifier = {
            width(100.percent)
            height(height)
            backgroundColor(CSSColors.Gray300)
            borderRadius(8.px)
        },
    ) {
        Text("Нет фото")
    }
}
