package my.drivebit.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.browser.window
import my.drivebit.design.CSSColors
import my.drivebit.design.CSSTypography
import my.drivebit.design.applyTypography
import my.drivebit.shared.storage.Storage
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.A
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import org.koin.compose.koinInject

private const val COOKIE_NOTICE_KEY = "cookie_notice_acknowledged"

@Composable
fun CookieConsentBanner() {
    val storage: Storage = koinInject()
    var visible by remember {
        mutableStateOf(!storage.contains(COOKIE_NOTICE_KEY))
    }

    if (!visible) return

    Div({
        style {
            position(Position.Fixed)
            property("left", "12px")
            property("bottom", "12px")
            property("z-index", "900")
            property("max-width", "min(340px, calc(100vw - 24px))")
            padding(10.px, 14.px)
            backgroundColor(Color("rgba(255, 255, 255, 0.96)"))
            property("border", "1px solid rgba(9, 5, 43, 0.08)")
            property("border-radius", "12px")
            property("box-shadow", "0 4px 20px rgba(9, 5, 43, 0.08)")
            display(DisplayStyle.Flex)
            alignItems(AlignItems.Center)
            gap(10.px)
            property("pointer-events", "auto")
        }
        attr("role", "note")
        attr("aria-label", "Уведомление о cookies")
    }) {
        Span({
            style {
                flex(1)
                applyTypography(CSSTypography.Styles.body)
                fontSize(12.px)
                lineHeight("1.4")
                color(CSSColors.Gray600)
            }
        }) {
            Text("Мы используем cookies. ")
            A(attrs = {
                attr("href", "/cookies")
                onClick { event ->
                    event.preventDefault()
                    window.location.href = "/cookies"
                }
                style {
                    color(CSSColors.Blue)
                    textDecoration("none")
                }
            }) {
                Text("Подробнее")
            }
        }
        Button(attrs = {
            onClick {
                storage.putString(COOKIE_NOTICE_KEY, "1")
                visible = false
            }
            style {
                flexShrink(0)
                border(0.px)
                property("border-radius", "8px")
                padding(6.px, 12.px)
                backgroundColor(CSSColors.Blue)
                color(CSSColors.White)
                applyTypography(CSSTypography.Styles.body)
                fontSize(12.px)
                fontWeight(CSSTypography.FontWeight.medium)
                property("cursor", "pointer")
                property("white-space", "nowrap")
            }
        }) {
            Text("Понятно")
        }
    }
}
