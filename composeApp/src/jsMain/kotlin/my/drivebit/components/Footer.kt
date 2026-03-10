package my.drivebit.components

import androidx.compose.runtime.Composable
import my.drivebit.design.CSSColors
import my.drivebit.design.CSSTypography
import my.drivebit.design.applyTypography
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.A
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text

private val FooterBg = Color("#101221")
private val FooterTextColor = CSSColors.White

@Composable
private fun FooterLink(href: String, text: String) {
    A(attrs = {
        attr("href", href)
        style {
            applyTypography(CSSTypography.Styles.caption)
            color(FooterTextColor)
            textDecoration("none")
            property("transition", "opacity 0.2s ease")
        }
    }) {
        Text(text)
    }
}

@Composable
private fun FooterCaption(text: String) {
    Span({
        style {
            applyTypography(CSSTypography.Styles.caption)
            color(FooterTextColor)
        }
    }) {
        Text(text)
    }
}

@Composable
private fun FooterPhoneLink() {
    A(attrs = {
        attr("href", "tel:+79268237180")
        style {
            applyTypography(CSSTypography.Styles.body)
            fontSize(CSSTypography.FontSize.xl)
            fontWeight(CSSTypography.FontWeight.medium)
            color(FooterTextColor)
            textDecoration("none")
            property("transition", "color 0.2s ease")
        }
    }) {
        Text("+7 (926) 823-71-80")
    }
}

@Composable
fun Footer() {
    Div({
        style {
            marginTop(24.px)
            padding(32.px, 24.px)
            backgroundColor(FooterBg)
            property("border-radius", "12px")
        }
    }) {
        Column(
            gap = 24.px,
            modifier = {
                alignItems(AlignItems.Center)
                width(100.percent)
            },
        ) {
            FooterPhoneLink()
            Row(
                gap = 16.px,
                alignItems = AlignItems.Center,
                modifier = { width(100.percent) },
            ) {
                Div({
                    style {
                        flex(1)
                        property("text-align", "left")
                    }
                }) {
                    FooterCaption("© DriveBit Аренда автомобилей от собственников")
                }
                FooterLink("/offer", "Оферта")
                Div({
                    style {
                        flex(1)
                        property("text-align", "right")
                    }
                }) {
                    FooterLink("/privacy", "Политика конфиденциальности")
                }
            }
        }
    }
}
