package my.drivebit.components

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.attributes.AttrsScope
import org.jetbrains.compose.web.css.AlignItems
import org.jetbrains.compose.web.css.CSSSizeValue
import org.jetbrains.compose.web.css.CSSUnit
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.FlexDirection
import org.jetbrains.compose.web.css.FlexWrap
import org.jetbrains.compose.web.css.JustifyContent
import org.jetbrains.compose.web.css.StyleScope
import org.jetbrains.compose.web.css.alignItems
import org.jetbrains.compose.web.css.display
import org.jetbrains.compose.web.css.flexDirection
import org.jetbrains.compose.web.css.flexWrap
import org.jetbrains.compose.web.css.gap
import org.jetbrains.compose.web.css.justifyContent
import org.jetbrains.compose.web.dom.Div
import org.w3c.dom.HTMLDivElement

@Composable
fun Row(
    gap: CSSSizeValue<*>? = null,
    flexWrap: FlexWrap? = null,
    justifyContent: JustifyContent? = null,
    alignItems: AlignItems? = null,
    modifier: (StyleScope.() -> Unit)? = null,
    attrs: (AttrsScope<HTMLDivElement>.() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Div({
        attrs?.invoke(this)
        style {
            display(DisplayStyle.Flex)
            flexDirection(FlexDirection.Row)
            gap?.let { gap(it) }
            flexWrap?.let { flexWrap(it) }
            justifyContent?.let { justifyContent(it) }
            alignItems?.let { alignItems(it) }
            modifier?.invoke(this)
        }
    }) {
        content()
    }
}

@Composable
fun Row(
    gap: CSSSizeValue<out CSSUnit.px>? = null,
    alignItems: AlignItems? = null,
    justifyContent: JustifyContent? = null,
    content: @Composable () -> Unit,
) {
    Div({
        style {
            display(DisplayStyle.Flex)
            flexDirection(FlexDirection.Row)
            gap?.let { gap(it) }
            alignItems?.let { alignItems(it) }
            justifyContent?.let { justifyContent(it) }
        }
    }) {
        content()
    }
}
