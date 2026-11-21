package my.drivebit.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.browser.window
import my.drivebit.design.CSSColors
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import org.w3c.dom.events.Event

@Composable
fun ToolbarBackArrow(
    title: String,
    onBackClick: () -> Unit,
    badges: List<String> = emptyList(),
) {
    var isMobile by remember { mutableStateOf(window.innerWidth <= 768) }
    DisposableEffect(Unit) {
        val onResize: (Event) -> Unit = {
            isMobile = window.innerWidth <= 768
        }
        window.addEventListener("resize", onResize)
        onDispose { window.removeEventListener("resize", onResize) }
    }

    Div({
        style {
            display(DisplayStyle.Flex)
            flexDirection(FlexDirection.Row)
            alignItems(AlignItems.Center)
            gap(12.px)
            width(100.percent)
            minWidth(0.px)
            property("box-sizing", "border-box")
        }
    }) {
        Div({
            onClick { onBackClick() }
            attr("aria-label", "Назад")
            style {
                display(DisplayStyle.Flex)
                alignItems(AlignItems.Center)
                justifyContent(JustifyContent.Center)
                width(40.px)
                height(40.px)
                borderRadius(12.px)
                cursor("pointer")
                backgroundColor(CSSColors.Gray7)
                property("flex-shrink", "0")
                property("transition", "background-color 0.15s ease")
                property("user-select", "none")
            }
            onMouseOver {
                (it.currentTarget as? org.w3c.dom.HTMLElement)?.style?.backgroundColor = "#e8eaef"
            }
            onMouseOut {
                (it.currentTarget as? org.w3c.dom.HTMLElement)?.style?.backgroundColor = CSSColors.Gray7String
            }
        }) {
            Span({
                style {
                    fontSize(18.px)
                    fontWeight(600)
                    color(CSSColors.Black)
                    lineHeight("1")
                }
            }) {
                Text("←")
            }
        }

        Div({
            style {
                display(DisplayStyle.Flex)
                flexDirection(FlexDirection.Row)
                alignItems(AlignItems.Center)
                gap(8.px)
                minWidth(0.px)
                flex(1)
                property("overflow", "hidden")
            }
        }) {
            Span({
                style {
                    fontSize(18.px)
                    fontWeight(600)
                    color(CSSColors.Black)
                    lineHeight("1.25")
                    property("overflow", "hidden")
                    property("text-overflow", "ellipsis")
                    property("white-space", "nowrap")
                    property("min-width", "0")
                }
            }) {
                Text(title)
            }
            if (badges.isNotEmpty()) {
                VerificationBadgeRow(
                    labels = badges,
                    compact = isMobile,
                )
            }
        }
    }
}
