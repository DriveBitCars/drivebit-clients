package my.drivebit.components

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div

@Composable
fun Spacer(spacerSize: CSSSizeValue<out CSSUnit.px> = 12.px) {
    Div({
        style {
            size(spacerSize)
        }
    }) {}
}

private fun StyleScope.size(value: CSSSizeValue<out CSSUnit.px>) {
    width(value)
    height(value)
}
