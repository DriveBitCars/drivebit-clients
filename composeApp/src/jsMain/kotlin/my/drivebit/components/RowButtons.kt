package my.drivebit.components

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div

@Composable
fun RowButtons(
    gap: CSSSizeValue<out CSSUnit.px> = 12.px,
    content: @Composable () -> Unit,
) {
    Div({
        style {
            display(DisplayStyle.Flex)
            flexDirection(FlexDirection.Row)
            gap(gap)
            justifyContent(JustifyContent.Center)
        }
    }) {
        content()
    }
}
