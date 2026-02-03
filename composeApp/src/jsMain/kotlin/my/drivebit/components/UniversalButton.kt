package my.drivebit.components

import androidx.compose.runtime.Composable
import my.drivebit.design.CSSColors
import org.jetbrains.compose.web.css.AlignItems
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.alignItems
import org.jetbrains.compose.web.css.backgroundColor
import org.jetbrains.compose.web.css.borderRadius
import org.jetbrains.compose.web.css.color
import org.jetbrains.compose.web.css.cursor
import org.jetbrains.compose.web.css.display
import org.jetbrains.compose.web.css.gap
import org.jetbrains.compose.web.css.padding
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.Div

@Composable
fun UniversalButton(
    isSelected: Boolean = false,
    onClick: () -> Unit,
    content: @Composable (Boolean) -> Unit,
) {
    Div({
        style {
            display(DisplayStyle.Flex)
            alignItems(AlignItems.Center)
            gap(8.px)
            padding(10.px, 10.px)
            borderRadius(8.px)
            cursor("pointer")
            backgroundColor(if (isSelected) CSSColors.Black else CSSColors.White)
            color(if (isSelected) CSSColors.White else CSSColors.Black)
            property("transition", "all 0.2s ease")
            property("border", "none")
        }
        classes("universal-button")
        onClick { onClick() }
    }) {
        content(isSelected)
    }
}
