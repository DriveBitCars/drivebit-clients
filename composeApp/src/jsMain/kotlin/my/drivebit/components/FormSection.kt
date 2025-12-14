package my.drivebit.components

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div

@Composable
fun FormSection(
    marginTop: CSSSizeValue<out CSSUnit.px> = 32.px,
    gap: CSSSizeValue<out CSSUnit.px> = 16.px,
    content: @Composable () -> Unit,
) {
    Div({
        style {
            marginTop(marginTop)
            display(DisplayStyle.Flex)
            flexDirection(FlexDirection.Column)
            gap(gap)
        }
    }) {
        content()
    }
}
