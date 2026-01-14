package my.drivebit.components

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div

@Composable
fun FormSection(
    marginTop: CSSSizeValue<out CSSUnit.px> = 32.px,
    gap: CSSSizeValue<out CSSUnit.px> = 16.px,
    listingContent: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Div({
        style {
            marginTop(marginTop)
            position(Position.Relative)
        }
    }) {
        Div({
            style {
                display(DisplayStyle.Flex)
                flexDirection(FlexDirection.Column)
                gap(gap)
            }
        }) {
            content()
        }

        listingContent?.let {
            Div({
                style {
                    position(Position.Absolute)
                    top(100.percent)
                    left(0.px)
                    right(0.px)
                    marginTop(8.px)
                    property("z-index", "1000")
                }
            }) {
                it()
            }
        }
    }
}
