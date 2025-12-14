package my.drivebit.components

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div

@Composable
fun ButtonContainer(
    id: String? = null,
    marginTop: CSSSizeValue<out CSSUnit.px> = 24.px,
    content: @Composable () -> Unit,
) {
    Div({
        id?.let { attr("id", it) }
        style {
            marginTop(marginTop)
        }
    }) {
        content()
    }
}
