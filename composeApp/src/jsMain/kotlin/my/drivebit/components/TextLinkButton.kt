package my.drivebit.components

import androidx.compose.runtime.Composable
import my.drivebit.design.CSSColors
import my.drivebit.design.CSSTypography
import my.drivebit.design.applyTypography
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text

@Composable
fun TextLinkButton(
    text: String,
    onClick: () -> Unit,
) {
    Span({
        onClick { onClick() }
        onMouseEnter {
            (it.target as org.w3c.dom.HTMLElement).style.setProperty(
                "text-decoration",
                "underline",
            )
        }
        onMouseLeave {
            (it.target as org.w3c.dom.HTMLElement).style.setProperty(
                "text-decoration",
                "none",
            )
        }
        style {
            applyTypography(CSSTypography.Styles.body)
            fontSize(CSSTypography.FontSize.sm)
            fontWeight(CSSTypography.FontWeight.medium)
            color(CSSColors.BlueRed)
            cursor("pointer")
        }
    }) {
        Text(text)
    }
}
