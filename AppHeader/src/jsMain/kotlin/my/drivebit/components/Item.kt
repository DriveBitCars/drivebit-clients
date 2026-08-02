package my.drivebit.components

import androidx.compose.runtime.Composable
import my.drivebit.design.CSSColors
import my.drivebit.design.CSSTypography
import my.drivebit.design.applyTypography
import org.jetbrains.compose.web.css.AlignItems
import org.jetbrains.compose.web.css.alignItems
import org.jetbrains.compose.web.css.backgroundColor
import org.jetbrains.compose.web.css.color
import org.jetbrains.compose.web.css.flex
import org.jetbrains.compose.web.css.height
import org.jetbrains.compose.web.css.marginRight
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text

@Composable
@Suppress("FunctionName")
fun Item(
    icon: String?,
    text: String,
    showDivider: Boolean = true,
) {
    Row(alignItems = AlignItems.Center) {
        if (icon != null) {
            Div({
                attr("role", "img")
                attr("aria-label", text)
                style {
                    width(24.px)
                    height(24.px)
                    marginRight(12.px)
                    flex("0 0 auto")
                    backgroundColor(CSSColors.Blue)
                    property("mask-image", "url($icon)")
                    property("mask-size", "contain")
                    property("mask-repeat", "no-repeat")
                    property("mask-position", "center")
                    property("-webkit-mask-image", "url($icon)")
                    property("-webkit-mask-size", "contain")
                    property("-webkit-mask-repeat", "no-repeat")
                    property("-webkit-mask-position", "center")
                }
            })
        }

        Span({
            style {
                applyTypography(CSSTypography.Styles.body)
                color(CSSColors.Gray600)
            }
        }) {
            Text(text)
        }
    }

    if (showDivider) {
        Divider()
    }
}
