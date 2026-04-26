package my.drivebit.screens

import androidx.compose.runtime.Composable
import my.drivebit.components.AppWithHeader
import my.drivebit.components.ToolbarBackArrow
import my.drivebit.design.CSSColors
import my.drivebit.design.CSSTypography
import my.drivebit.design.applyTypography
import my.drivebit.navigation.LocalNavigationController
import org.jetbrains.compose.web.css.color
import org.jetbrains.compose.web.css.fontSize
import org.jetbrains.compose.web.css.lineHeight
import org.jetbrains.compose.web.css.padding
import org.jetbrains.compose.web.css.paddingTop
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text

@Composable
fun AiSearchPage() {
    val navigationController = LocalNavigationController.current!!
    AppWithHeader {
        Div({
            style {
                width(100.percent)
                padding(20.px)
                property("max-width", "640px")
                property("margin", "0 auto")
            }
        }) {
            ToolbarBackArrow(
                title = "ИИ-поиск",
                onBackClick = { navigationController.goBack() },
            )
            Div({
                style {
                    paddingTop(24.px)
                }
            }) {
                Span({
                    style {
                        applyTypography(CSSTypography.Styles.body)
                        fontSize(CSSTypography.FontSize.xxxl)
                        color(CSSColors.Gray600)
                        lineHeight("1.5")
                    }
                }) {
                    Text("Функция в разработке — скоро заработает.")
                }
            }
        }
    }
}
