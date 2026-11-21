package my.drivebit.shell

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import kotlinx.browser.window
import my.drivebit.components.AppContainer
import my.drivebit.components.ButterMenu
import my.drivebit.components.CityDisplay
import my.drivebit.components.MenuUserButton
import my.drivebit.components.ResponsiveContainer
import my.drivebit.design.CSSColors
import my.drivebit.viewmodels.ButterViewModel
import org.jetbrains.compose.web.css.AlignItems
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.alignItems
import org.jetbrains.compose.web.css.backgroundColor
import org.jetbrains.compose.web.css.borderRadius
import org.jetbrains.compose.web.css.color
import org.jetbrains.compose.web.css.cursor
import org.jetbrains.compose.web.css.display
import org.jetbrains.compose.web.css.fontSize
import org.jetbrains.compose.web.css.fontWeight
import org.jetbrains.compose.web.css.gap
import org.jetbrains.compose.web.css.height
import org.jetbrains.compose.web.css.padding
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.textDecoration
import org.jetbrains.compose.web.dom.A
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text
import org.koin.compose.koinInject
import org.w3c.dom.events.Event

@Composable
fun HeaderInteractiveCompose() {
    val butterViewModel: ButterViewModel = koinInject()
    val isMobile = window.innerWidth <= 768

    DisposableEffect(butterViewModel) {
        val openProfile: (Event) -> Unit = {
            butterViewModel.open()
        }
        window.asDynamic().drivebitOpenButter = {
            butterViewModel.open()
        }
        window.asDynamic().drivebitCloseButter = {
            butterViewModel.close()
        }
        window.addEventListener("drivebit:open-profile-menu", openProfile)
        onDispose {
            window.removeEventListener("drivebit:open-profile-menu", openProfile)
            js("delete window.drivebitOpenButter")
            js("delete window.drivebitCloseButter")
        }
    }

    Div({
        style {
            display(DisplayStyle.Flex)
            alignItems(AlignItems.Center)
            gap(16.px)
            property("position", "relative")
        }
        classes("drivebit-header-compose-inner")
    }) {
        CityDisplay()
        if (!isMobile) {
            A(attrs = {
                attr("href", "tel:+74958775051")
                classes("drivebit-header-phone")
                style {
                    color(CSSColors.White)
                    fontSize(14.px)
                    fontWeight("700")
                    textDecoration("none")
                    property("font-family", "\"Inter Tight\", Inter, sans-serif")
                    property("white-space", "nowrap")
                }
            }) {
                Text("+7 (495) 877-50-51")
            }
            A(attrs = {
                attr("href", "/login-by-phone")
                classes("drivebit-header-login")
                style {
                    display(DisplayStyle.Flex)
                    alignItems(AlignItems.Center)
                    height(40.px)
                    padding(0.px, 18.px)
                    borderRadius(16.px)
                    backgroundColor(Color("#0D2551"))
                    color(CSSColors.White)
                    fontSize(14.px)
                    fontWeight("500")
                    textDecoration("none")
                    cursor("pointer")
                    property("white-space", "nowrap")
                }
            }) {
                Text("Войти")
            }
        } else {
            MenuUserButton {
                window.dispatchEvent(Event("drivebit:toggle-site-menu"))
            }
        }
    }
    ButterMenu()
}

@Composable
fun PageContentShell(
    fullBleed: Boolean = false,
    content: @Composable () -> Unit,
) {
    ResponsiveContainer { isMobile ->
        AppContainer(isMobile = isMobile, fullBleed = fullBleed) {
            content()
        }
    }
}

@Composable
fun AppWithHeader(content: @Composable () -> Unit) = PageContentShell(content = content)
