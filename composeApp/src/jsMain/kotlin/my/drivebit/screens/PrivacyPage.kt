package my.drivebit.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.browser.window
import my.drivebit.components.AppWithHeader
import my.drivebit.design.CSSTypography
import my.drivebit.design.applyTypography
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.A
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text

@Composable
fun PrivacyPage() {
    var privacyText by remember { mutableStateOf<String?>(null) }
    var loadError by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        window
            .fetch("/privacy.txt")
            .then { it.text() }
            .then { privacyText = it }
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
            A(attrs = {
                attr("href", "/")
                onClick {
                    it.preventDefault()
                    window.location.href = "/"
                }
                style {
                    applyTypography(CSSTypography.Styles.body)
                    color(
                        org.jetbrains.compose.web.css
                            .Color("#1a73e8"),
                    )
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
                Text("Политика конфиденциальности")
            }
            when {
                loadError -> {
                    P({
                        style {
                            color(
                                org.jetbrains.compose.web.css
                                    .Color("#c5221f"),
                            )
                        }
                    }) {
                        Text("Не удалось загрузить текст. Попробуйте позже.")
                    }
                }
                privacyText != null -> {
                    Div({
                        style {
                            applyTypography(CSSTypography.Styles.body)
                            property("white-space", "pre-wrap")
                            property("word-break", "break-word")
                            lineHeight("1.6")
                        }
                    }) {
                        Text(privacyText!!)
                    }
                }
                else -> {
                    P({
                        style {
                            color(
                                org.jetbrains.compose.web.css
                                    .Color("#5f6368"),
                            )
                        }
                    }) {
                        Text("Загрузка…")
                    }
                }
            }
        }
    }
}
