package my.drivebit.components

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div

@Composable
fun Row(
    gap: CSSSizeValue<out CSSUnit.px>? = null,
    alignItems: AlignItems? = null,
    justifyContent: JustifyContent? = null,
    content: @Composable () -> Unit,
) {
    Div({
        style {
            display(DisplayStyle.Flex)
            flexDirection(FlexDirection.Row)
            gap?.let { gap(it) }
            alignItems?.let { alignItems(it) }
            justifyContent?.let { justifyContent(it) }
        }
    }) {
        content()
    }
}
