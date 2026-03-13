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
) {
    Button({
        onClick { onClick() }
        style {
            width(292.px)
            height(90.px)
            backgroundColor(PromoRectButtonBlue)
            borderRadius(20.px)
            border(0.px)
            color(CSSColors.White)
            cursor("pointer")
            applyTypography(CSSTypography.Styles.button)
            fontSize(CSSTypography.FontSize.base)
            fontWeight(CSSTypography.FontWeight.semibold)
            property("box-sizing", "border-box")
        }
    }) {
        Text(text)
    }
}
