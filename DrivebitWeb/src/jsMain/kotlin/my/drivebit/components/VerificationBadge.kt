package my.drivebit.components

import androidx.compose.runtime.Composable
import my.drivebit.design.CSSColors
import org.jetbrains.compose.web.css.AlignItems
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.FlexDirection
import org.jetbrains.compose.web.css.FlexWrap
import org.jetbrains.compose.web.css.JustifyContent
import org.jetbrains.compose.web.css.alignItems
import org.jetbrains.compose.web.css.backgroundColor
import org.jetbrains.compose.web.css.borderRadius
import org.jetbrains.compose.web.css.color
import org.jetbrains.compose.web.css.display
import org.jetbrains.compose.web.css.flexDirection
import org.jetbrains.compose.web.css.flexWrap
import org.jetbrains.compose.web.css.fontSize
import org.jetbrains.compose.web.css.fontWeight
import org.jetbrains.compose.web.css.gap
import org.jetbrains.compose.web.css.height
import org.jetbrains.compose.web.css.justifyContent
import org.jetbrains.compose.web.css.lineHeight
import org.jetbrains.compose.web.css.padding
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text

@Composable
fun VerificationBadge(text: String) {
    val displayText = text.uppercase()
    Span({
        style {
            padding(6.px, 10.px)
            borderRadius(999.px)
            property("border", "1px solid ${CSSColors.BlueString}")
            backgroundColor(Color("#E6EEF9"))
            color(CSSColors.Blue)
            fontSize(11.px)
            fontWeight("600")
            property("letter-spacing", "0.04em")
            lineHeight("1.2")
            property("white-space", "nowrap")
            property("flex-shrink", "0")
        }
    }) {
        Text(displayText)
    }
}

/** Compact verified mark when there is no room for the full label next to a name. */
@Composable
fun VerificationCheckBadge(title: String = "Проверенный пользователь") {
    Span({
        attr("title", title)
        attr("aria-label", title)
        style {
            property("display", "inline-flex")
            alignItems(AlignItems.Center)
            justifyContent(JustifyContent.Center)
            width(22.px)
            height(22.px)
            borderRadius(999.px)
            property("border", "1px solid ${CSSColors.BlueString}")
            backgroundColor(Color("#E6EEF9"))
            color(CSSColors.Blue)
            fontSize(13.px)
            fontWeight("700")
            lineHeight("1")
            property("flex-shrink", "0")
        }
    }) {
        Text("✓")
    }
}

@Composable
fun VerificationBadgeRow(
    labels: List<String>,
    compact: Boolean = false,
) {
    if (labels.isEmpty()) return
    Div({
        style {
            display(DisplayStyle.Flex)
            flexDirection(FlexDirection.Row)
            flexWrap(if (compact) FlexWrap.Nowrap else FlexWrap.Wrap)
            alignItems(AlignItems.Center)
            gap(6.px)
            property("flex-shrink", "0")
        }
    }) {
        if (compact) {
            VerificationCheckBadge(title = labels.joinToString(", "))
        } else {
            labels.forEach { label ->
                VerificationBadge(label)
            }
        }
    }
}
