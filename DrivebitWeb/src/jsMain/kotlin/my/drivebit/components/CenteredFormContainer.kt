package my.drivebit.components

import androidx.compose.runtime.Composable
import my.drivebit.components.Column
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div

@Composable
fun CenteredFormContainer(
    maxWidth: CSSSizeValue<out CSSUnit.px> = 400.px,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = {
            alignItems(AlignItems.Center)
            justifyContent(JustifyContent.FlexStart)
            minHeight(80.vh)
            paddingTop(40.px)
        },
    ) {
        Div({
            style {
                width(100.percent)
                maxWidth(maxWidth)
            }
        }) {
            content()
        }
    }
}
