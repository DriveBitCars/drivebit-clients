package my.drivebit.components

import androidx.compose.runtime.Composable
import my.drivebit.design.CSSColors
import my.drivebit.design.CSSTypography
import my.drivebit.design.applyTypography
import org.jetbrains.compose.web.css.AlignItems
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.alignItems
import org.jetbrains.compose.web.css.backgroundColor
import org.jetbrains.compose.web.css.color
import org.jetbrains.compose.web.css.display
import org.jetbrains.compose.web.css.height
import org.jetbrains.compose.web.css.marginRight
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Img
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text

@Composable
@Suppress("FunctionName")
fun Item(
    icon: String?,
    text: String,
    showDivider: Boolean = true,
) {
    Div({
        style {
            display(DisplayStyle.Flex)
            alignItems(AlignItems.Center)
        }
    }) {
        if (icon != null) {
            Img(
                src = icon,
                alt = text,
                attrs = {
                    style {
                        width(24.px)
                        height(24.px)
                        property("object-fit", "contain")
                        marginRight(12.px)
                    }
                },
            )
        }

        Span({
            style {
                applyTypography(CSSTypography.Styles.body)
                color(CSSColors.Black)
            }
        }) {
            Text(text)
        }
    }

    if (showDivider) {
        Div({
            style {
                width(100.percent)
                height(1.px)
                backgroundColor(CSSColors.Gray300)
            }
        })
    }
}
