package my.drivebit.shell

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import kotlinx.browser.window
import my.drivebit.components.AppContainer
import my.drivebit.components.ButterMenu
import my.drivebit.components.CityDisplay
import my.drivebit.components.HeaderNav
import my.drivebit.components.HeaderRow
import my.drivebit.components.Logo
import my.drivebit.components.MenuUserButton
import my.drivebit.components.ResponsiveContainer
import my.drivebit.components.Row
import my.drivebit.design.CSSColors
import my.drivebit.shared.storage.Storage
import my.drivebit.viewmodels.ButterViewModel
import my.drivebit.viewmodels.UnreadMessagesViewModel
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.A
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text
import org.koin.compose.koinInject

@Composable
fun StandaloneAppHeader() {
    ResponsiveContainer { isMobile ->
        AppContainer(isMobile = isMobile) {
            AppHeaderChrome(isMobile = isMobile)
        }
    }
}

@Composable
fun PageContentShell(content: @Composable () -> Unit) {
    ResponsiveContainer { isMobile ->
        AppContainer(isMobile = isMobile) {
            content()
        }
    }
}

@Composable
fun AppWithHeader(content: @Composable () -> Unit) {
    if (hasExternalAppHeaderMount()) {
        PageContentShell(content)
        return
    }
    val butterViewModel: ButterViewModel = koinInject()

    ResponsiveContainer { isMobile ->
        AppContainer(isMobile = isMobile) {
            Div({
                style {
                    display(DisplayStyle.Flex)
                    flexDirection(FlexDirection.Column)
                    minHeight(100.vh)
                }
            }) {
                Div({
                    style {
                        flex(1)
                        display(DisplayStyle.Flex)
                        flexDirection(FlexDirection.Column)
                    }
                }) {
                    AppHeaderChrome(
                        isMobile = isMobile,
                        butterViewModel = butterViewModel,
                    )

                    content()
                }
            }
        }
    }
}

@Composable
private fun AppHeaderChrome(
    isMobile: Boolean,
    butterViewModel: ButterViewModel? = null,
) {
    val butter: ButterViewModel = butterViewModel ?: koinInject()
    val unreadMessagesViewModel: UnreadMessagesViewModel = koinInject()
    val storage: Storage = koinInject()
    val hasUnreadState = unreadMessagesViewModel.hasUnread.collectAsState()

    if (storage.isLogined() && hasUnreadState.value) {
        UnreadMessagesBanner()
    }

    HeaderRow {
        Logo()
        if (!isMobile) {
            HeaderNav()
        }
        Row(
            alignItems = AlignItems.Center,
            gap = 12.px,
        ) {
            CityDisplay()
            MenuUserButton(butter::onClick)
        }
        ButterMenu()
    }
    if (isMobile) {
        Div({
            style {
                display(DisplayStyle.Flex)
                justifyContent(JustifyContent.Center)
                flexWrap(FlexWrap.Wrap)
                gap(8.px)
                padding(0.px, 0.px, 12.px, 0.px)
                marginBottom(8.px)
            }
        }) {
            HeaderNav()
        }
    }
}

@Composable
private fun UnreadMessagesBanner() {
    A(
        attrs = {
            attr("href", "/chats")
            onClick { event ->
                event.preventDefault()
                window.location.href = "/chats"
            }
            style {
                display(DisplayStyle.Block)
                backgroundColor(CSSColors.Black)
                color(CSSColors.White)
                padding(8.px)
                textAlign("center")
                property("text-decoration", "none")
                cursor("pointer")
            }
        },
    ) {
        Text("У Вас есть непрочитанные сообщения")
    }
}
