package my.drivebit.components

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.css.AlignItems
import org.jetbrains.compose.web.css.CSSSizeValue
import org.jetbrains.compose.web.css.CSSUnit
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.FlexDirection
import org.jetbrains.compose.web.css.FlexWrap
import org.jetbrains.compose.web.css.StyleScope
import org.jetbrains.compose.web.css.alignItems
import org.jetbrains.compose.web.css.display
import org.jetbrains.compose.web.css.flexDirection
import org.jetbrains.compose.web.css.flexWrap
import org.jetbrains.compose.web.css.gap
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.Div

/**
 * Horizontal layout that wraps to the next line when space runs out (flex row + wrap).
 */
@Composable
fun FlowRow(
    gap: CSSSizeValue<out CSSUnit.px>,
    alignItems: AlignItems = AlignItems.Center,
    modifier: (StyleScope.() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Div({
        style {
            display(DisplayStyle.Flex)
            flexDirection(FlexDirection.Row)
            flexWrap(FlexWrap.Wrap)
            gap(gap)
            alignItems(alignItems)
            width(100.percent)
            modifier?.invoke(this)
        }
    }) {
        content()
    }
}
