package my.drivebit.screens

import androidx.compose.runtime.Composable
import kotlinx.browser.window
import my.drivebit.components.AppWithHeader
import my.drivebit.design.CSSColors
import my.drivebit.design.CSSTypography
import my.drivebit.design.applyTypography
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.A
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text

@Composable
fun ContactsPage() {
    AppWithHeader {
        Div({
            style {
                padding(24.px)
                maxWidth(800.px)
                property("margin", "0 auto")
            }
        }) {
            A(attrs = {
                attr("href", "/")
                onClick {
                    it.preventDefault()
                    window.location.href = "/"
                }
                style {
                    applyTypography(CSSTypography.Styles.body)
                    color(CSSColors.Blue)
                    textDecoration("none")
                    property("margin-bottom", "16px")
                    display(DisplayStyle.InlineBlock)
                }
            }) {
                Text("← На главную")
            }
            P({
                style {
                    applyTypography(CSSTypography.Styles.body)
                    fontSize(CSSTypography.FontSize.xxl)
                    fontWeight(CSSTypography.FontWeight.semibold)
                    property("margin-bottom", "24px")
                }
            }) {
                Text("Контакты")
            }
            P({
                style {
                    applyTypography(CSSTypography.Styles.body)
                    fontWeight(CSSTypography.FontWeight.semibold)
                    property("margin-bottom", "8px")
                }
            }) {
                Text("Оператор платформы")
            }
            P({
                style {
                    applyTypography(CSSTypography.Styles.body)
                    property("margin-bottom", "16px")
                    lineHeight("1.6")
                }
            }) {
                Text("ООО «ДрайвБит»")
            }
            P({
                style {
                    applyTypography(CSSTypography.Styles.body)
                    property("margin-bottom", "4px")
                    lineHeight("1.6")
                }
            }) {
                Text("ОГРН: 1267700092813")
            }
            P({
                style {
                    applyTypography(CSSTypography.Styles.body)
                    property("margin-bottom", "16px")
                    lineHeight("1.6")
                }
            }) {
                Text("ИНН: 9727127969")
            }
            P({
                style {
                    applyTypography(CSSTypography.Styles.body)
                    fontWeight(CSSTypography.FontWeight.semibold)
                    property("margin-bottom", "8px")
                }
            }) {
                Text("Почтовый адрес")
            }
            P({
                style {
                    applyTypography(CSSTypography.Styles.body)
                    property("margin-bottom", "24px")
                    lineHeight("1.6")
                }
            }) {
                Text("117461, г. Москва, ул. Каховка, д. 33-1")
            }
            P({
                style {
                    applyTypography(CSSTypography.Styles.body)
                    fontWeight(CSSTypography.FontWeight.semibold)
                    property("margin-bottom", "8px")
                }
            }) {
                Text("Контактные данные для пользователей")
            }
            P({
                style {
                    applyTypography(CSSTypography.Styles.body)
                    property("margin-bottom", "4px")
                    lineHeight("1.6")
                }
            }) {
                Text("Сайт платформы: ")
                A(attrs = {
                    attr("href", "https://drivebit.ru")
                    attr("target", "_blank")
                    attr("rel", "noopener noreferrer")
                    style {
                        color(CSSColors.Blue)
                        textDecoration("none")
                    }
                }) {
                    Text("https://drivebit.ru")
                }
            }
            P({
                style {
                    applyTypography(CSSTypography.Styles.body)
                    lineHeight("1.6")
                }
            }) {
                Text("Электронная почта: ")
                A(attrs = {
                    attr("href", "mailto:info@drivebit.ru")
                    style {
                        color(CSSColors.Blue)
                        textDecoration("none")
                    }
                }) {
                    Text("info@drivebit.ru")
                }
            }
        }
    }
}
