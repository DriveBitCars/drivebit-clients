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

private fun normalizeOfferFormatting(text: String): String {
    var t = text.trimStart()
    if (t.startsWith("# ")) t = t.removePrefix("# ")
    t = t.replace("ОФЕРТАна", "ОФЕРТА на")
    t = t.replace("»Дата", "» Дата")
    t = t.replace("г.1.", "г. 1.")
    t = t.replace("документа1.1.", "документа 1.1.")
    t = t.replace(").ВАЖНО:", ").\n\nВАЖНО:")
    t = t.replace("ПРОСТЫМИ СЛОВАМИ:", "\n\nПРОСТЫМИ СЛОВАМИ:")
    t = t.replace(Regex("---"), "\n\n---\n\n")
    return t
}

@Composable
fun OfferPage() {
    var offerText by remember { mutableStateOf<String?>(null) }
    var loadError by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        window
            .fetch("/offer.txt")
            .then { it.text() }
            .then { offerText = it }
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
                Text("Публичная оферта")
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
                        Text("Не удалось загрузить текст оферты. Попробуйте позже.")
                    }
                }
                offerText != null -> {
                    Div({
                        style {
                            applyTypography(CSSTypography.Styles.body)
                            property("white-space", "pre-wrap")
                            property("word-break", "break-word")
                            lineHeight("1.6")
                        }
                    }) {
                        Text(normalizeOfferFormatting(offerText!!))
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
