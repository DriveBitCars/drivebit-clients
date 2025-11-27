package my.drivebit.components

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div

@Composable
fun Spacer(height: CSSSizeValue<out CSSUnit.px>) {
    Div({
        style {
            height(height)
        }
    }) {}
}
