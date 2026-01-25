package my.drivebit.components

import androidx.compose.runtime.Composable
import my.drivebit.design.CSSColors
import my.drivebit.design.CSSTypography
import my.drivebit.design.applyTypography
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text

@Composable
fun Hero() {
    Div({
        style {
            flex(1)
            display(DisplayStyle.Flex)
            flexDirection(FlexDirection.Column)
            justifyContent(JustifyContent.Center)
            alignItems(AlignItems.Center)
            textAlign("center")
            padding(0.px, 48.px)
        }
    }) {
        Div({
            style {
                applyTypography(CSSTypography.Styles.body)
                fontSize(CSSTypography.FontSize.lg)
                fontWeight(CSSTypography.FontWeight.bold)
                color(CSSColors.Black)
                lineHeight("1.2")
             //   marginBottom(12.px)
            }
        }) {
            Text("Арендуй автомобиль до 40% дешевле проката")
        }
        Div({
            style {
                applyTypography(CSSTypography.Styles.body)
                fontSize(CSSTypography.FontSize.xs)
                fontWeight(CSSTypography.FontWeight.normal)
                color(CSSColors.Black)
                lineHeight("1.5")
                property("max-width", "1600px")
            }
        }) {
            Text(
                "Выбираешь конкретную машину с реальными фото и комплектацией. " +
                    "Мы соединяем с проверенным владельцем, держим залог и помогаем в спорах.",
            )
        }
    }
}
