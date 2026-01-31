package my.drivebit.components

import androidx.compose.runtime.Composable
import my.drivebit.components.Column
import my.drivebit.design.CSSColors
import my.drivebit.design.CSSTypography
import my.drivebit.design.applyTypography
import org.jetbrains.compose.web.css.AlignItems
import org.jetbrains.compose.web.css.JustifyContent
import org.jetbrains.compose.web.css.alignItems
import org.jetbrains.compose.web.css.color
import org.jetbrains.compose.web.css.flex
import org.jetbrains.compose.web.css.fontSize
import org.jetbrains.compose.web.css.fontWeight
import org.jetbrains.compose.web.css.justifyContent
import org.jetbrains.compose.web.css.lineHeight
import org.jetbrains.compose.web.css.maxWidth
import org.jetbrains.compose.web.css.padding
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.textAlign
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text

@Composable
fun Hero(isMobile: Boolean = false) {
    Column(
        modifier = {
            flex(1)
            justifyContent(JustifyContent.Center)
            alignItems(AlignItems.Center)
            textAlign("center")
            padding(0.px, if (isMobile) 0.px else 48.px)
        },
    ) {
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
                maxWidth(1600.px)
            }
        }) {
            Text(
                "Выбираешь конкретную машину с реальными фото и комплектацией. " +
                    "Мы соединяем с проверенным владельцем, держим залог и помогаем в спорах.",
            )
        }
    }
}
