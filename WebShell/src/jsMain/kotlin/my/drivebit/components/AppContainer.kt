package my.drivebit.components

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.css.Position
import org.jetbrains.compose.web.dom.Div

@Composable
fun AppContainer(
    isMobile: Boolean,
    content: @Composable () -> Unit,
) {
    Div({
        style {
            position(Position.Relative)
            paddingTop(if (isMobile) 8.px else 20.px)
            paddingBottom(if (isMobile) 16.px else 40.px)
            paddingLeft(if (isMobile) 8.px else 40.px)
            paddingRight(if (isMobile) 8.px else 40.px)
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
