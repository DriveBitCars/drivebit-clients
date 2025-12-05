package my.drivebit.components

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div

@Composable
fun Column(
    gap: CSSSizeValue<out CSSUnit.px>? = null,
    marginBottom: CSSSizeValue<out CSSUnit.px>? = null,
    content: @Composable () -> Unit,
) {
    Div({
        style {
            display(DisplayStyle.Flex)
            flexDirection(FlexDirection.Column)
            gap?.let { gap(it) }
            marginBottom?.let { marginBottom(it) }
        }
    }) {
        content()
    }
}
