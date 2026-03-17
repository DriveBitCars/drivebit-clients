package my.drivebit.components

import androidx.compose.runtime.Composable
import my.drivebit.design.CSSTypography
import my.drivebit.components.ResponsiveContainer
import my.drivebit.components.Row
import my.drivebit.design.applyTypography
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.A
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text

private val FooterBg = Color("#09052B")
private val FooterTextColor = Color("#ffffff")

@Composable
private fun FooterLink(href: String, text: String, centered: Boolean = false) {
    A(attrs = {
        attr("href", href)
        style {
            applyTypography(CSSTypography.Styles.body)
            fontSize(CSSTypography.FontSize.sm)
            color(FooterTextColor)
            textDecoration("none")
            property("transition", "opacity 0.2s ease")
            if (centered) {
                property("display", "block")
                property("text-align", "center")
            }
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
            paddingTop(32.px)
            paddingBottom(32.px)
            paddingLeft(40.px)
            paddingRight(40.px)
            property("box-sizing", "border-box")
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
            ResponsiveContainer { isMobile ->
                if (isMobile) {
                    Column(
                        gap = 8.px,
                        modifier = {
                            alignItems(AlignItems.Center)
                            width(100.percent)
                            property("text-align", "center")
                        },
                    ) {
                        Span({
                            style {
                                applyTypography(CSSTypography.Styles.body)
                                fontSize(CSSTypography.FontSize.sm)
                                color(FooterTextColor)
                                property("text-align", "center")
                                property("display", "block")
                            }
                        }) {
                            Text("© DriveBit Аренда автомобилей от собственников")
                        }
                        FooterLink("/offer", "Оферта", centered = true)
                        FooterLink("/privacy", "Политика конфиденциальности", centered = true)
                    }
                } else {
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
                                    fontSize(CSSTypography.FontSize.sm)
                                    color(FooterTextColor)
                                }
                            }) {
                                Text("© DriveBit Аренда автомобилей от собственников")
                            }
                        }
                        Row(justifyContent = JustifyContent.Center) {
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
    }
}
