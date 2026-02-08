package my.drivebit.components

import androidx.compose.runtime.Composable
import my.drivebit.design.CSSColors
import my.drivebit.design.CSSTypography
import my.drivebit.design.applyTypography
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text

@Composable
fun PriceFilterTitle() {
    Span({
        style {
            applyTypography(CSSTypography.Styles.body)
            fontSize(CSSTypography.FontSize.base)
            fontWeight(CSSTypography.FontWeight.semibold)
            color(CSSColors.Black)
            textAlign("center")
            width(100.percent)
        }
    }) {
        Text("Общая цена до налогов")
    }
}
