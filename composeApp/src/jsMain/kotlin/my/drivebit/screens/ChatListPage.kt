package my.drivebit.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.delay
import androidx.compose.runtime.getValue
import kotlinx.browser.window
import kotlinx.datetime.Instant
import my.drivebit.components.CenteredFormContainer
import my.drivebit.components.Column
import my.drivebit.components.Loader
import my.drivebit.components.MessageTextWithDealsLink
import my.drivebit.components.ParticipantAvatar
import my.drivebit.components.PageHeader
import my.drivebit.components.PageWithLogo
import my.drivebit.components.Row
import my.drivebit.components.TextError
import my.drivebit.components.TextSmartHeader
import my.drivebit.design.CSSColors
import my.drivebit.network.services.ChatListDto
import my.drivebit.utils.formatRelativeTime
import my.drivebit.viewmodels.ChatListViewModel
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import org.koin.compose.koinInject

@Composable
fun ChatListPage() {
    val viewModel: ChatListViewModel = koinInject()
    val chats by viewModel.chats.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadChats()
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(10_000)
            viewModel.refreshChats()
        }
    }

    PageWithLogo {
        CenteredFormContainer(maxWidth = 1200.px) {
            PageHeader {
                TextSmartHeader("Входящие")
            }

            when {
                isLoading -> Loader()
                error != null -> TextError(error ?: "Ошибка")
                chats.isEmpty() -> {
                    Div({
                        style {
                            padding(32.px)
                            color(CSSColors.Gray600)
                        }
                    }) {
                        Text("У вас пока нет сообщений")
                    }
                }
                else -> {
                    Column(gap = 8.px, modifier = { width(100.percent) }) {
                        chats.forEach { chat ->
                            ChatListItem(
                                chat = chat,
                                onClick = {
                                    window.location.href = "/chat?id=${chat.id}"
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatListItem(
    chat: ChatListDto,
    onClick: () -> Unit,
) {
    val participantName = chat.participant.name?.takeIf { it.isNotBlank() } ?: "Собеседник"
    val lastMessageText = chat.lastMessage?.text?.takeIf { it.isNotBlank() } ?: ""
    val relativeTime =
        runCatching {
            chat.lastMessage?.createdAt?.let { formatRelativeTime(Instant.parse(it)) } ?: ""
        }.getOrElse { "" }

    Div({
        style {
            cursor("pointer")
            display(DisplayStyle.Flex)
            flexDirection(FlexDirection.Row)
            alignItems(AlignItems.Center)
            padding(16.px)
            borderRadius(12.px)
            property("transition", "background-color 0.2s ease")
        }
        onClick { onClick() }
    }) {
        Row(
            gap = 12.px,
            alignItems = AlignItems.Center,
            modifier = { width(100.percent) },
        ) {
            ParticipantAvatar(
                userId = chat.participant.id,
                name = participantName,
                initialAvatarUrl = chat.participant.avatar,
            )
            Column(gap = 4.px, modifier = { width(100.percent) }) {
                Row(
                    gap = 8.px,
                    alignItems = AlignItems.Center,
                    modifier = { width(100.percent) },
                ) {
                    Span({
                        style {
                            fontSize(16.px)
                            fontWeight("600")
                            color(CSSColors.Black)
                        }
                    }) {
                        Text(participantName)
                    }
                    if (chat.unreadCount > 0) {
                        Span({
                            style {
                                padding(4.px, 8.px)
                                borderRadius(50.percent)
                                backgroundColor(CSSColors.Blue)
                                color(CSSColors.White)
                                fontSize(12.px)
                                fontWeight("600")
                            }
                        }) {
                            Text("${chat.unreadCount}")
                        }
                    }
                }
                Row(
                    gap = 8.px,
                    alignItems = AlignItems.Center,
                    modifier = { width(100.percent) },
                ) {
                    Span({
                        style {
                            fontSize(14.px)
                            color(CSSColors.Gray600)
                            property("overflow", "hidden")
                            property("text-overflow", "ellipsis")
                            property("white-space", "nowrap")
                            flex(1)
                        }
                    }) {
                        MessageTextWithDealsLink(
                            text = lastMessageText.ifBlank { "Нет сообщений" },
                            stopPropagation = true,
                        )
                    }
                    Span({
                        style {
                            fontSize(12.px)
                            color(CSSColors.Gray600)
                            property("flex-shrink", "0")
                        }
                    }) {
                        Text(relativeTime)
                    }
                }
            }
        }
    }
}

