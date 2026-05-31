package my.drivebit.components

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div

@Composable
fun PageHeader(
    marginTop: CSSSizeValue<out CSSUnit.px> = 0.px,
    content: @Composable () -> Unit,
) {
    Div({
        style {
            marginTop(marginTop)
        }
    }) {
        content()
    }
}
