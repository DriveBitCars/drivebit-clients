package my.drivebit.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.browser.document
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
    val scrollbarWidth: String,
    val msOverflowStyle: String,
    val cssClass: String,
) {
    val wraps: Boolean get() = flexWrap != "nowrap"
}

fun horizontalScrollRowStyle(): HorizontalScrollRowStyle =
    HorizontalScrollRowStyle(
        flexWrap = "nowrap",
        overflowX = "auto",
        childFlexShrink = "0",
        scrollbarWidth = "none",
        msOverflowStyle = "none",
        cssClass = "drivebit-horizontal-scroll",
    )

private const val HORIZONTAL_SCROLL_STYLE_ID = "drivebit-horizontal-scroll-style"

private fun ensureHorizontalScrollHiddenScrollbarCss(cssClass: String) {
    val css =
        """
        .$cssClass {
          scrollbar-width: none;
          -ms-overflow-style: none;
        }
        .$cssClass::-webkit-scrollbar {
          display: none;
          width: 0;
          height: 0;
        }
        """.trimIndent()
    val existing = document.getElementById(HORIZONTAL_SCROLL_STYLE_ID)
    if (existing != null) {
        existing.textContent = css
        return
    }
    val style = document.createElement("style")
    style.id = HORIZONTAL_SCROLL_STYLE_ID
    style.textContent = css
    document.head?.appendChild(style)
}

@Composable
fun HorizontalScrollRow(
    gap: CSSSizeValue<out CSSUnit.px>,
    alignItems: AlignItems = AlignItems.Center,
    modifier: (StyleScope.() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val styleSpec = horizontalScrollRowStyle()
    LaunchedEffect(styleSpec.cssClass) {
        ensureHorizontalScrollHiddenScrollbarCss(styleSpec.cssClass)
    }
    Div({
        classes(styleSpec.cssClass)
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
            property("scrollbar-width", styleSpec.scrollbarWidth)
            property("-ms-overflow-style", styleSpec.msOverflowStyle)
            modifier?.invoke(this)
        }
    }) {
        content()
    }
}
