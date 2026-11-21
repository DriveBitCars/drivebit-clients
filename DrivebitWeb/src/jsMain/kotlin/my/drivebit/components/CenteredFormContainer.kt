package my.drivebit.components

import androidx.compose.runtime.Composable
import my.drivebit.components.Column
import my.drivebit.design.CSSColors
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div

@Composable
fun CenteredFormContainer(
    maxWidth: CSSSizeValue<out CSSUnit.px> = 440.px,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = {
            alignItems(AlignItems.Center)
            justifyContent(JustifyContent.FlexStart)
            flexGrow(1)
            width(100.percent)
            minHeight(100.percent)
            property("min-height", "calc(100vh - 150px)")
            paddingTop(40.px)
            paddingBottom(48.px)
            paddingLeft(16.px)
            paddingRight(16.px)
            property("box-sizing", "border-box")
            backgroundColor(CSSColors.Gray7)
        },
    ) {
        Div({
            style {
                width(100.percent)
                maxWidth(maxWidth)
                minWidth(0.px)
                backgroundColor(CSSColors.White)
                borderRadius(24.px)
                padding(32.px)
                property("box-sizing", "border-box")
                property("overflow-x", "hidden")
                property("box-shadow", "0 8px 32px rgba(21, 32, 50, 0.06)")
                property("border", "1px solid ${CSSColors.Gray6String}")
            }
        }) {
            content()
        }
    }
}
