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

data class HorizontalScrollRowStyle(
    val flexWrap: String,
    val overflowX: String,
    val childFlexShrink: String,
) {
    val wraps: Boolean get() = flexWrap != "nowrap"
}

fun horizontalScrollRowStyle(): HorizontalScrollRowStyle =
    HorizontalScrollRowStyle(
        flexWrap = "nowrap",
        overflowX = "auto",
        childFlexShrink = "0",
    )

@Composable
fun HorizontalScrollRow(
    gap: CSSSizeValue<out CSSUnit.px>,
    alignItems: AlignItems = AlignItems.Center,
    modifier: (StyleScope.() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val styleSpec = horizontalScrollRowStyle()
    Div({
        style {
            display(DisplayStyle.Flex)
            flexDirection(FlexDirection.Row)
            flexWrap(FlexWrap.Nowrap)
            gap(gap)
            alignItems(alignItems)
            width(100.percent)
            property("overflow-x", styleSpec.overflowX)
            property("overflow-y", "hidden")
            property("-webkit-overflow-scrolling", "touch")
            modifier?.invoke(this)
        }
    }) {
        content()
    }
}
