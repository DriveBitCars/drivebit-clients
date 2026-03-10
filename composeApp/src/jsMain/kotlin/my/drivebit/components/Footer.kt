package my.drivebit.components

import androidx.compose.runtime.Composable
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
    val footerText = Color.white

    Div({
        style {
            marginTop(24.px)
            padding(32.px, 24.px)
            backgroundColor(footerBg)
            property("border-radius", "12px")
        }
    }) {
        Div({
            style {
                display(DisplayStyle.Flex)
                flexDirection(FlexDirection.Column)
                alignItems(AlignItems.Center)
                gap(24.px)
            }
        }) {
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
            Div({
                style {
                    display(DisplayStyle.Flex)
                    width(100.percent)
                    justifyContent(JustifyContent.SpaceBetween)
                    alignItems(AlignItems.Center)
                    flexWrap(FlexWrap.Wrap)
                    gap(12.px)
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
                A(attrs = {
                    attr("href", "/offer")
                    style {
                        applyTypography(CSSTypography.Styles.caption)
                        color(footerText)
                        textDecoration("none")
                        property("transition", "color 0.2s ease")
                    }
                }) {
                    Text("Оферта")
                }
                A(attrs = {
                    attr("href", "/privacy")
                    style {
                        applyTypography(CSSTypography.Styles.caption)
                        color(footerText)
                        textDecoration("none")
                        property("transition", "color 0.2s ease")
                    }
                }) {
                    Text("Политика конфиденциальности")
                }
            }
        }
    }
}
