package my.drivebit.components

import androidx.compose.runtime.Composable
import my.drivebit.design.CSSTypography
import my.drivebit.design.applyTypography
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.css.color
import org.jetbrains.compose.web.css.letterSpacing
import org.jetbrains.compose.web.css.lineHeight
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text

@Composable
fun TextSmartHeader(text: String) {
    Span({
        classes("db-h3")
        style {
            applyTypography(CSSTypography.Styles.h3)
            // Form title: slightly tighter than full-page H3 for card layouts
            property("font-size", "28px")
            lineHeight("1.25")
            color(Color("#152032"))
            letterSpacing(0.px)
            property("display", "block")
        }
    }) {
        Text(text)
    }
}
