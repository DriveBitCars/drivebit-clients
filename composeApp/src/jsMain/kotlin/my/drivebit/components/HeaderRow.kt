package my.drivebit.components

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div

@Composable
fun HeaderRow(
    marginBottom: CSSSizeValue<out CSSUnit.px> = 20.px,
    content: @Composable () -> Unit,
) {
    Div({
        style {
            display(DisplayStyle.Flex)
            justifyContent(JustifyContent.SpaceBetween)
            alignItems(AlignItems.Center)
            marginBottom(marginBottom)
        }
    }) {
        content()
    }
}
