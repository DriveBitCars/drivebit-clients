package my.drivebit.components

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.css.Position
import org.jetbrains.compose.web.css.color
import org.jetbrains.compose.web.css.fontFamily
import org.jetbrains.compose.web.css.paddingBottom
import org.jetbrains.compose.web.css.paddingLeft
import org.jetbrains.compose.web.css.paddingRight
import org.jetbrains.compose.web.css.paddingTop
import org.jetbrains.compose.web.css.position
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.Div

@Composable
fun AppContainer(
    isMobile: Boolean,
    fullBleed: Boolean = false,
    content: @Composable () -> Unit,
) {
    Div({
        style {
            position(Position.Relative)
            if (fullBleed) {
                paddingTop(0.px)
                paddingBottom(0.px)
                paddingLeft(0.px)
                paddingRight(0.px)
                property("max-width", "none")
                property("width", "100%")
                property("margin", "0")
            } else {
                paddingTop(if (isMobile) 8.px else 20.px)
                paddingBottom(if (isMobile) 16.px else 40.px)
                paddingLeft(if (isMobile) 16.px else 40.px)
                paddingRight(if (isMobile) 16.px else 40.px)
                property("max-width", "1200px")
                property("margin", "0 auto")
            }
            fontFamily("\"Roboto\", sans-serif")
            color(Color("#152032"))
            property("min-width", "320px")
            property("box-sizing", "border-box")
        }
        classes("app-container")
        if (fullBleed) {
            classes("app-container--full-bleed")
        }
    }) {
        content()
    }
}
