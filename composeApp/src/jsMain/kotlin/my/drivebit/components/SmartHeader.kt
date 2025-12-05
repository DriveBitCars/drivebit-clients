package my.drivebit.components

import androidx.compose.runtime.Composable
import my.drivebit.design.CSSColors
import my.drivebit.design.CSSTypography
import my.drivebit.design.applyTypography
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text

@Composable
fun SmartHeader(text: String) {
    Span({
        style {
            applyTypography(CSSTypography.Styles.body)
            fontSize(CSSTypography.FontSize.xxl)
            fontWeight(CSSTypography.FontWeight.bold)
            color(CSSColors.Black)
            letterSpacing(0.5.px)
            lineHeight("1.2")
        }
    }) {
        Text(text)
    }
}
