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
            property("left", "50%")
            property("transform", "translateX(-50%)")
            property("bottom", "24px")
            property("z-index", "1400")
            property("max-width", "min(480px, calc(100vw - 32px))")
            width(100.percent)
            padding(12.px, 16.px)
            backgroundColor(CSSColors.White)
            property("border", "1px solid rgba(21, 32, 50, 0.12)")
            property("border-radius", "16px")
            property("box-shadow", "0 8px 32px rgba(0, 24, 69, 0.18)")
            display(DisplayStyle.Flex)
            alignItems(AlignItems.Center)
            gap(12.px)
            property("pointer-events", "auto")
            property("box-sizing", "border-box")
        }
        attr("role", "note")
        attr("aria-label", "Уведомление о cookies")
        classes("drivebit-cookie-banner")
    }) {
        Span({
            style {
                flex(1)
                applyTypography(CSSTypography.Styles.body)
                fontSize(13.px)
                lineHeight("1.4")
                color(Color("#152032"))
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
                    textDecoration("underline")
                }
            }) {
                Text("Подробнее")
            }
        }
        Button(attrs = {
            classes("db-btn", "db-btn--sm", "db-btn--primary", "drivebit-cookie-btn")
            onClick {
                storage.putString(COOKIE_NOTICE_KEY, "1")
                visible = false
            }
            style {
                flexShrink(0)
                border(0.px)
                property("border-radius", "12px")
                height(32.px)
                padding(0.px, 14.px)
                backgroundColor(CSSColors.Blue)
                color(CSSColors.White)
                fontFamily("\"Roboto\", sans-serif")
                fontSize(13.px)
                fontWeight(CSSTypography.FontWeight.medium)
                property("line-height", "1")
                property("display", "inline-flex")
                property("align-items", "center")
                property("justify-content", "center")
                property("cursor", "pointer")
                property("white-space", "nowrap")
                property("box-sizing", "border-box")
                property("vertical-align", "middle")
            }
        }) {
            Text("Понятно")
        }
    }
}
