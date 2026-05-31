package my.drivebit.components

import androidx.compose.runtime.Composable
import my.drivebit.design.CSSColors
import my.drivebit.design.CSSTypography
import my.drivebit.design.applyTypography
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text

@Composable
fun TextSmallBodyGray(text: String) {
    Span({
        style {
            applyTypography(CSSTypography.Styles.body)
            fontSize(CSSTypography.FontSize.sm)
            fontWeight(CSSTypography.FontWeight.medium)
            color(CSSColors.Gray600)
            letterSpacing(0.3.px)
            opacity(0.8)
        }
    }) {
        Text(text)
    }
}

@Composable
fun TextSmallBodyBlack(text: String) {
    Span({
        style {
            applyTypography(CSSTypography.Styles.body)
            fontSize(CSSTypography.FontSize.sm)
            fontWeight(CSSTypography.FontWeight.medium)
            color(CSSColors.Black)
            letterSpacing(0.3.px)
            opacity(0.8)
        }
    }) {
        Text(text)
    }
}
