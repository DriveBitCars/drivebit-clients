package my.drivebit.components

import androidx.compose.runtime.Composable
import my.drivebit.design.CSSColors
import my.drivebit.design.CSSTypography
import my.drivebit.design.applyTypography
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Text

private val PromoRectButtonBlue: CSSColorValue = Color("#2962FF")

@Composable
fun PromoRectButton(
    text: String,
    onClick: () -> Unit,
    compact: Boolean = false,
) {
    Button({
        onClick { onClick() }
        style {
            if (compact) {
                width(100.percent)
                height(72.px)
                fontSize(CSSTypography.FontSize.sm)
            } else {
                width(292.px)
                height(90.px)
                fontSize(CSSTypography.FontSize.base)
            }
            backgroundColor(PromoRectButtonBlue)
            borderRadius(20.px)
            border(0.px)
            color(CSSColors.White)
            cursor("pointer")
            applyTypography(CSSTypography.Styles.button)
            fontWeight(CSSTypography.FontWeight.semibold)
            property("box-sizing", "border-box")
        }
    }) {
        Text(text)
    }
}
