package my.drivebit.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import my.drivebit.design.CSSColors
import my.drivebit.shared.storage.Storage
import my.drivebit.viewmodels.ButterViewModel
import my.drivebit.viewmodels.UnreadMessagesViewModel
import kotlinx.browser.window
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.A
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text
import org.koin.compose.koinInject

@Composable
fun AppWithHeader(content: @Composable () -> Unit) {
    val butterViewModel: ButterViewModel = koinInject()
    val unreadMessagesViewModel: UnreadMessagesViewModel = koinInject()
    val storage: Storage = koinInject()
    val hasUnreadState = unreadMessagesViewModel.hasUnread.collectAsState()

    ResponsiveContainer { isMobile ->
        AppContainer {
            if (storage.isLogined() && hasUnreadState.value) {
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
                    if (isMobile) {
                        Div({
                            style {
                                marginBottom(12.px)
                            }
                        }) {
                            Hero(isMobile)
                        }
                    }

                    HeaderRow {
                        Logo()

                        if (!isMobile) {
                            Hero(isMobile)
                        }

                        Row(
                            alignItems = AlignItems.Center,
                            gap = 12.px,
                        ) {
                            CityDisplay()
                            MenuUserButton(butterViewModel::onClick)
                        }

                        ButterMenu()
                    }

                    content()
                }

                Footer()
            }
        }
    }
}
