package my.drivebit.components

import androidx.compose.runtime.Composable
import my.drivebit.design.CSSTypography
import my.drivebit.design.applyTypography
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.A
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text

private val FooterBg = Color("#09052B")
private val FooterTextColor = Color("#ffffff")

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
fun Footer() {
    Div({
        style {
            marginTop(24.px)
            padding(32.px, 24.px)
            backgroundColor(FooterBg)
            property("border-radius", "24px")
            property("width", "100%")
            property("margin-left", "auto")
            property("margin-right", "auto")
        }
    }) {
        Column(
            gap = 32.px,
            modifier = {
                alignItems(AlignItems.Center)
                width(100.percent)
            },
        ) {
            A(attrs = {
                attr("href", "tel:+79268237180")
                style {
                    applyTypography(CSSTypography.Styles.body)
                    fontSize(CSSTypography.FontSize.xl)
                    fontWeight(CSSTypography.FontWeight.medium)
                    color(FooterTextColor)
                    textDecoration("none")
                }
            }) {
                Text("+7 (926) 823-71-80")
            }
            Row(
                alignItems = AlignItems.Center,
                modifier = { width(80.percent) },
            ) {
                Row(
                    gap = 8.px,
                    alignItems = AlignItems.Center,
                    modifier = { flex(1) },
                ) {
                    Span({
                        style {
                            applyTypography(CSSTypography.Styles.body)
                            fontSize(32.px)
                            color(FooterTextColor)
                        }
                    }) {
                        Text("©")
                    }
                    Span({
                        style {
                            applyTypography(CSSTypography.Styles.body)
                            fontSize(15.px)
                            color(FooterTextColor)
                        }
                    }) {
                        Text("DriveBit Аренда автомобилей от собственников")
                    }
                }
                Row(
                    justifyContent = JustifyContent.Center,
                ) {
                    FooterLink("/offer", "Оферта")
                }
                Row(
                    justifyContent = JustifyContent.FlexEnd,
                    modifier = { flex(1) },
                ) {
                    FooterLink("/privacy", "Политика конфиденциальности")
                }
            }
        }
    }
}
