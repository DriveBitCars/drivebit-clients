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
    val footerBg = rgb(26, 26, 26)
    val footerText = rgb(220, 220, 220)

    Div({
        style {
            marginTop(24.px)
            padding(16.px, 0.px)
            backgroundColor(footerBg)
            property("border-radius", "12px")
        }
    }) {
        Div({
            style {
                display(DisplayStyle.Flex)
                flexDirection(FlexDirection.Column)
                alignItems(AlignItems.Center)
                justifyContent(JustifyContent.Center)
                gap(8.px)
                flexWrap(FlexWrap.Wrap)
            }
        }) {
            A(attrs = {
                attr("href", "tel:+79268237180")
                style {
                    applyTypography(CSSTypography.Styles.body)
                    fontSize(CSSTypography.FontSize.lg)
                    fontWeight(CSSTypography.FontWeight.medium)
                    color(footerText)
                    textDecoration("none")
                    property("transition", "color 0.2s ease")
                }
            }) {
                Text("+7 (926) 823-7180")
            }
            Span({
                style {
                    applyTypography(CSSTypography.Styles.caption)
                    color(rgb(160, 160, 160))
                }
            }) {
                Text("DriveBit · Аренда автомобилей от собственников")
            }
        }
    }
}
