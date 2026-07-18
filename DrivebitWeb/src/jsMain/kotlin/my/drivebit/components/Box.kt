package my.drivebit.components

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.attributes.AttrsScope
import org.jetbrains.compose.web.css.Position
import org.jetbrains.compose.web.css.StyleScope
import org.jetbrains.compose.web.css.left
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.position
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.top
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.Div
import org.w3c.dom.HTMLDivElement

@Composable
fun Box(
    modifier: (StyleScope.() -> Unit)? = null,
    attrs: (AttrsScope<HTMLDivElement>.() -> Unit)? = null,
    underneath: @Composable () -> Unit,
    overlay: @Composable () -> Unit,
) {
    Div({
        attrs?.invoke(this)
        style {
            position(Position.Relative)
            width(100.percent)
            property("isolation", "isolate")
            modifier?.invoke(this)
        }
    }) {
        underneath()
        overlay()
    }
}

@Composable
fun BoxOverlay(content: @Composable () -> Unit) {
    Div({
        style {
            position(Position.Absolute)
            top(0.px)
            left(0.px)
            width(100.percent)
            property("z-index", "10")
            property("pointer-events", "none")
            property("box-sizing", "border-box")
        }
    }) {
        Div({
            style {
                property("pointer-events", "auto")
                width(100.percent)
                property("max-width", "400px")
                property("box-sizing", "border-box")
            }
        }) {
            content()
        }
    }
}
