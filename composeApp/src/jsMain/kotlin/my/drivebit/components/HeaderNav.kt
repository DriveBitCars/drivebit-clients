package my.drivebit.components

import androidx.compose.runtime.Composable
import kotlinx.browser.window
import my.drivebit.design.CSSColors
import my.drivebit.design.CSSTypography
import my.drivebit.design.applyTypography
import my.drivebit.navigation.LocalNavigationController
import my.drivebit.shared.storage.Storage
import my.drivebit.utils.REDIRECT_PATH
import my.drivebit.utils.encodeUrlParameter
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import org.koin.compose.koinInject

@Composable
fun HeaderNav() {
    val navigationController = LocalNavigationController.current
    val storage: Storage = koinInject()

    Row(
        gap = 20.px,
        alignItems = AlignItems.Center,
    ) {
        HeaderNavLink("Аренда авто") {
            navigationController?.navigateTo("/search")
        }
        HeaderNavLink("Сдать авто") {
            if (storage.isLogined()) {
                window.location.href = "/list-your-car.html"
            } else {
                val redirect = "/list-your-car.html".encodeUrlParameter()
                window.location.href = "/login-by-phone?$REDIRECT_PATH=$redirect"
            }
        }
        HeaderNavLink("Контакты") {
            navigationController?.navigateTo("/contacts")
        }
    }
}

@Composable
private fun HeaderNavLink(
    text: String,
    onClick: () -> Unit,
) {
    Span({
        onClick { onClick() }
        onMouseEnter {
            (it.target as? org.w3c.dom.HTMLElement)?.style?.setProperty(
                "text-decoration",
                "underline",
            )
        }
        onMouseLeave {
            (it.target as? org.w3c.dom.HTMLElement)?.style?.setProperty(
                "text-decoration",
                "none",
            )
        }
        style {
            applyTypography(CSSTypography.Styles.body)
            fontSize(CSSTypography.FontSize.base)
            fontWeight(CSSTypography.FontWeight.medium)
            color(CSSColors.Black)
            cursor("pointer")
            property("text-decoration", "none")
        }
    }) {
        Text(text)
    }
}
