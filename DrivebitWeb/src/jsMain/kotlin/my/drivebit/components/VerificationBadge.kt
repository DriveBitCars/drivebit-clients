package my.drivebit.components

import androidx.compose.runtime.Composable
import my.drivebit.design.CSSColors
import org.jetbrains.compose.web.css.AlignItems
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.FlexDirection
import org.jetbrains.compose.web.css.FlexWrap
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
import org.jetbrains.compose.web.css.padding
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text

@Composable
fun VerificationBadge(text: String) {
    val displayText = text.uppercase()
    Span({
        style {
            padding(4.px, 8.px)
            borderRadius(6.px)
            backgroundColor(CSSColors.Gray300)
            color(CSSColors.Gray600)
            fontSize(12.px)
            fontWeight("600")
        }
    }) {
        Text(displayText)
    }
}

@Composable
fun VerificationBadgeRow(labels: List<String>) {
    if (labels.isEmpty()) return
    Div({
        style {
            display(DisplayStyle.Flex)
            flexDirection(FlexDirection.Row)
            flexWrap(FlexWrap.Wrap)
            alignItems(AlignItems.Center)
            gap(6.px)
        }
    }) {
        labels.forEach { label ->
            VerificationBadge(label)
        }
    }
}
