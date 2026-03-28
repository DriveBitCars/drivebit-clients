package my.drivebit.screens

import androidx.compose.runtime.Composable
import kotlinx.browser.window
import my.drivebit.components.ActionButton
import my.drivebit.components.AppWithHeader
import my.drivebit.components.Column
import my.drivebit.design.CSSColors
import my.drivebit.design.CSSTypography
import my.drivebit.design.applyTypography
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text

@Composable
fun PaymentSuccessPage() {
    AppWithHeader {
        Div({
            style {
                padding(24.px)
                maxWidth(560.px)
                property("margin", "0 auto")
            }
        }) {
            Column(
                gap = 20.px,
                modifier = {
                    alignItems(AlignItems.Center)
                    property("text-align", "center")
                },
            ) {
                Div({
                    style {
                        width(72.px)
                        height(72.px)
                        borderRadius(50.percent)
                        backgroundColor(CSSColors.Green)
                        color(CSSColors.White)
                        display(DisplayStyle.Flex)
                        alignItems(AlignItems.Center)
                        justifyContent(JustifyContent.Center)
                        fontSize(CSSTypography.FontSize.xxxl)
                        fontWeight(CSSTypography.FontWeight.bold)
                    }
                }) {
                    Text("✓")
                }

                P({
                    style {
                        applyTypography(CSSTypography.Styles.body)
                        fontSize(CSSTypography.FontSize.xxl)
                        fontWeight(CSSTypography.FontWeight.semibold)
                        property("margin", "0")
                    }
                }) {
                    Text("Оплата прошла успешно")
                }

                P({
                    style {
                        applyTypography(CSSTypography.Styles.body)
                        color(CSSColors.Gray600)
                        lineHeight("1.6")
                        property("margin", "0")
                    }
                }) {
                    Text(
                        "Спасибо! Платёж принят. Если это бронирование, статус обновится в разделе «Мои бронирования».",
                    )
                }

                Div({
                    style {
                        width(100.percent)
                        maxWidth(400.px)
                        marginTop(8.px)
                    }
                }) {
                    ActionButton(
                        enabledColor = CSSColors.Blue,
                        text = "Входящие",
                        onClick = { window.location.href = "/chats" },
                    )
                }
            }
        }
    }
}
