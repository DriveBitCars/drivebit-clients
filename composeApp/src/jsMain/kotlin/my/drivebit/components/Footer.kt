package my.drivebit.components

import androidx.compose.runtime.Composable
import my.drivebit.design.CSSTypography
import my.drivebit.design.applyTypography
import my.drivebit.resources.ImagePaths
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.A
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Img
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text

private val FooterBg = Color("#101221")
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
                modifier = { width(100.percent) },
            ) {
                Row(
                    gap = 8.px,
                    alignItems = AlignItems.Center,
                    modifier = { flex(1) },
                ) {
                    Img(
                        src = ImagePaths.LOGOS_DRIVEBIT_FOOTER_SVG,
                        alt = "DriveBit",
                        attrs = {
                            style {
                                width(28.px)
                                height(28.px)
                                property("object-fit", "contain")
                            }
                        },
                    )
                    Span({
                        style {
                            applyTypography(CSSTypography.Styles.caption)
                            color(FooterTextColor)
                        }
                    }) {
                        Text("DriveBit Аренда автомобилей от собственников")
                    }
                }
                Row(
                    justifyContent = JustifyContent.Center,
                    modifier = { flex(1) },
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
