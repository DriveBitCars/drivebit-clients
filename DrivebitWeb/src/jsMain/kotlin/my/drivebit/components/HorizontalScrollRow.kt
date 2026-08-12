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
    val touchAction: String,
    val overscrollBehaviorX: String,
    val minWidth: String,
    val trackCssClass: String,
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
        touchAction = "pan-x",
        overscrollBehaviorX = "contain",
        minWidth = "0",
        trackCssClass = "drivebit-horizontal-scroll-track",
    )

private const val HORIZONTAL_SCROLL_STYLE_ID = "drivebit-horizontal-scroll-style"

private fun ensureHorizontalScrollHiddenScrollbarCss(
    cssClass: String,
    trackCssClass: String,
    touchAction: String,
    overscrollBehaviorX: String,
) {
    val css =
        """
        .$cssClass {
          scrollbar-width: none;
          -ms-overflow-style: none;
          touch-action: $touchAction;
          overscroll-behavior-x: $overscrollBehaviorX;
          -webkit-overflow-scrolling: touch;
          max-width: 100%;
          min-width: 0;
          box-sizing: border-box;
        }
        .$cssClass::-webkit-scrollbar {
          display: none;
          width: 0;
          height: 0;
        }
        .$trackCssClass {
          display: flex;
          flex: 0 0 auto;
          flex-wrap: nowrap;
          align-items: center;
          justify-content: flex-start;
          width: max-content;
          min-width: 0;
          max-width: none;
          box-sizing: border-box;
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
    LaunchedEffect(styleSpec.cssClass, styleSpec.trackCssClass) {
        ensureHorizontalScrollHiddenScrollbarCss(
            cssClass = styleSpec.cssClass,
            trackCssClass = styleSpec.trackCssClass,
            touchAction = styleSpec.touchAction,
            overscrollBehaviorX = styleSpec.overscrollBehaviorX,
        )
    }
    Div({
        classes(styleSpec.cssClass)
        attr("role", "group")
        attr("tabindex", "0")
        attr("aria-label", "Прокрутка фильтров")
        style {
            display(DisplayStyle.Flex)
            flexDirection(FlexDirection.Row)
            flexWrap(FlexWrap.Nowrap)
            alignItems(alignItems)
            width(100.percent)
            property("max-width", "100%")
            property("min-width", styleSpec.minWidth)
            property("overflow-x", styleSpec.overflowX)
            property("overflow-y", "hidden")
            property("-webkit-overflow-scrolling", "touch")
            property("touch-action", styleSpec.touchAction)
            property("overscroll-behavior-x", styleSpec.overscrollBehaviorX)
            property("scrollbar-width", styleSpec.scrollbarWidth)
            property("-ms-overflow-style", styleSpec.msOverflowStyle)
            property("box-sizing", "border-box")
            modifier?.invoke(this)
        }
    }) {
        Div({
            classes(styleSpec.trackCssClass)
            style {
                display(DisplayStyle.Flex)
                flexDirection(FlexDirection.Row)
                flexWrap(FlexWrap.Nowrap)
                gap(gap)
                alignItems(alignItems)
                property("flex", "0 0 auto")
                property("width", "max-content")
                property("min-width", styleSpec.minWidth)
                property("max-width", "none")
                property("box-sizing", "border-box")
            }
        }) {
            content()
        }
    }
}
