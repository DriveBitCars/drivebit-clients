package my.drivebit.components

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div

@Composable
fun ErrorContainer(
    padding: CSSSizeValue<out CSSUnit.px> = 24.px,
    content: @Composable () -> Unit,
) {
    Div({
        style {
            textAlign("center")
            padding(padding)
        }
    }) {
        content()
    }
}
