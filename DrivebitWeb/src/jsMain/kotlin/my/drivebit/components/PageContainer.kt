package my.drivebit.components

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div

@Composable
fun PageContainer(
    maxWidth: CSSSizeValue<out CSSUnit.px> = 800.px,
    padding: CSSSizeValue<out CSSUnit.px> = 24.px,
    content: @Composable () -> Unit,
) {
    Div({
        style {
            padding(padding)
            maxWidth(maxWidth)
            margin(0.px)
            property("margin-left", "auto")
            property("margin-right", "auto")
        }
    }) {
        content()
    }
}
