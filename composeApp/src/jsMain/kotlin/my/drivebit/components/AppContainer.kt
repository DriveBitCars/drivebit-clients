package my.drivebit.components

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.css.Position
import org.jetbrains.compose.web.dom.Div

@Composable
fun AppContainer(
    isMobile: Boolean = false,
    content: @Composable () -> Unit,
) {
    val horizontal = if (isMobile) 8.px else 40.px
    Div({
        style {
            position(Position.Relative)
            paddingTop(20.px)
            paddingBottom(40.px)
            paddingLeft(horizontal)
            paddingRight(horizontal)
            fontFamily("system-ui, -apple-system, sans-serif")
            property("min-width", "320px")
            property("max-width", "1200px")
            property("margin", "0 auto")
            property("box-sizing", "border-box")
        }
        classes("app-container")
    }) {
        content()
    }
}
