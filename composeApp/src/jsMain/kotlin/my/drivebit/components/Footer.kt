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

@Composable
fun Footer() {
    val footerBg = Color("#101221")
    val footerText = CSSColors.White

    Div({
        style {
            marginTop(24.px)
            padding(32.px, 24.px)
            backgroundColor(footerBg)
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
            A(attrs = {
                attr("href", "tel:+79268237180")
                style {
                    applyTypography(CSSTypography.Styles.body)
                    fontSize(20.px)
                    fontWeight(CSSTypography.FontWeight.medium)
                    color(footerText)
                    textDecoration("none")
                    property("transition", "color 0.2s ease")
                }
            }) {
                Text("+7 (926) 823-71-80")
            }
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
                    Span({
                        style {
                            applyTypography(CSSTypography.Styles.caption)
                            color(footerText)
                        }
                    }) {
                        Text("© DriveBit Аренда автомобилей от собственников")
                    }
                }
                A(attrs = {
                    attr("href", "/offer")
                    style {
                        applyTypography(CSSTypography.Styles.caption)
                        color(footerText)
                        textDecoration("none")
                        property("transition", "opacity 0.2s ease")
                    }
                }) {
                    Text("Оферта")
                }
                Div({
                    style {
                        flex(1)
                        property("text-align", "right")
                    }
                }) {
                    A(attrs = {
                        attr("href", "/privacy")
                        style {
                            applyTypography(CSSTypography.Styles.caption)
                            color(footerText)
                            textDecoration("none")
                            property("transition", "opacity 0.2s ease")
                        }
                    }) {
                        Text("Политика конфиденциальности")
                    }
                }
            }
        }
    }
}
