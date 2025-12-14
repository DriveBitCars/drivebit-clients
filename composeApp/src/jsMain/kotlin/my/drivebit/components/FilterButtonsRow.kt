package my.drivebit.components

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div

@Composable
fun FilterButtonsRow(
    gap: CSSSizeValue<out CSSUnit.px> = 12.px,
    marginBottom: CSSSizeValue<out CSSUnit.px> = 20.px,
    content: @Composable () -> Unit,
) {
    Div({
        style {
            display(DisplayStyle.Flex)
            gap(gap)
            alignItems(AlignItems.Center)
            justifyContent(JustifyContent.Center)
            marginBottom(marginBottom)
            flexWrap(FlexWrap.Wrap)
        }
    }) {
        content()
    }
}
