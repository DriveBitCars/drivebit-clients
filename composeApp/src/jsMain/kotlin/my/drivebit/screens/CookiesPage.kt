package my.drivebit.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.browser.window
import my.drivebit.shell.AppWithHeader
import my.drivebit.design.CSSTypography
import my.drivebit.design.applyTypography
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text

@Composable
fun CookiesPage() {
    var cookiesText by remember { mutableStateOf<String?>(null) }
    var loadError by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        window
            .fetch("/cookies.txt")
            .then { it.text() }
            .then { cookiesText = it }
            .catch { loadError = true }
    }

    AppWithHeader {
        Div({
            style {
                padding(24.px)
                maxWidth(800.px)
                property("margin", "0 auto")
            }
        }) {
            P({
                style {
                    applyTypography(CSSTypography.Styles.body)
                    fontSize(CSSTypography.FontSize.xxl)
                    fontWeight(CSSTypography.FontWeight.semibold)
                    property("margin-bottom", "24px")
                }
            }) {
                Text("Политика использования cookies")
            }
            when {
                loadError -> {
                    P({
                        style {
                            color(Color("#c5221f"))
                        }
                    }) {
                        Text("Не удалось загрузить текст. Попробуйте позже.")
                    }
                }
                cookiesText != null -> {
                    Div({
                        style {
                            applyTypography(CSSTypography.Styles.body)
                            property("white-space", "pre-wrap")
                            property("word-break", "break-word")
                            lineHeight("1.6")
                        }
                    }) {
                        Text(cookiesText!!)
                    }
                }
                else -> {
                    P({
                        style {
                            color(Color("#5f6368"))
                        }
                    }) {
                        Text("Загрузка…")
                    }
                }
            }
        }
    }
}
