package my.drivebit.components

import androidx.compose.runtime.Composable
import my.drivebit.design.CSSColors
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div

@Composable
fun Loader(size: CSSSizeValue<out CSSUnit.px> = 48.px) {
    Div({
        style {
            display(DisplayStyle.Flex)
            justifyContent(JustifyContent.Center)
            alignItems(AlignItems.Center)
            padding(48.px)
        }
    }) {
        Div({
            style {
                width(size)
                height(size)
                border(3.px, LineStyle.Solid, CSSColors.Gray300)
                property("border-top-color", CSSColors.BlueRedString)
                borderRadius(50.percent)
                property("animation", "spin 1s linear infinite")
            }
        }) {}
    }
}
