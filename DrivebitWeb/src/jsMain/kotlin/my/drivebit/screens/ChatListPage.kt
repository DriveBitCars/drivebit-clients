package my.drivebit.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.delay
import kotlinx.datetime.Instant
import my.drivebit.components.CenteredFormContainer
import my.drivebit.components.Column
import my.drivebit.components.Loader
import my.drivebit.components.PageHeader
import my.drivebit.shell.PageWithLogo
import my.drivebit.components.ParticipantAvatar
import my.drivebit.components.Row
import my.drivebit.components.TextError
import my.drivebit.components.TextSmartHeader
import my.drivebit.components.VerificationBadgeRow
import my.drivebit.design.CSSColors
import my.drivebit.network.services.ChatListDto
import my.drivebit.utils.formatRelativeTime
import my.drivebit.viewmodels.ChatListViewModel
import my.drivebit.viewmodels.VerificationLabels
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import org.koin.compose.koinInject
import org.w3c.dom.events.Event

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

    DisposableEffect(Unit) {
        document.body?.classList?.add("drivebit-chats-scope")
        document.body?.classList?.remove("drivebit-chat-thread")
        onDispose {
            val path = window.location.pathname
            if (!path.startsWith("/chat")) {
                document.body?.classList?.remove("drivebit-chats-scope")
            }
        }
    }

    PageWithLogo(fullBleed = true) {
        CenteredFormContainer(maxWidth = 720.px) {
            PageHeader {
                TextSmartHeader("Сообщения")
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
                    Column(
                        gap = 4.px,
                        modifier = {
                            width(100.percent)
                            minWidth(0.px)
                            property("box-sizing", "border-box")
                        },
                    ) {
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
            width(100.percent)
            minWidth(0.px)
            padding(14.px, 12.px)
            borderRadius(16.px)
            property("box-sizing", "border-box")
            property("overflow", "hidden")
            property("transition", "background-color 0.2s ease")
        }
        onClick { onClick() }
    }) {
        Row(
            gap = 12.px,
            alignItems = AlignItems.Center,
            modifier = {
                width(100.percent)
                minWidth(0.px)
                property("box-sizing", "border-box")
            },
        ) {
            ParticipantAvatar(
                userId = chat.participant.id,
                name = participantName,
                initialAvatarUrl = chat.participant.avatar,
            )
            Column(
                gap = 4.px,
                modifier = {
                    flex(1)
                    minWidth(0.px)
                    property("overflow", "hidden")
                    property("box-sizing", "border-box")
                },
            ) {
                val verificationLabels =
                    VerificationLabels.forUser(
                        isPassportVerified = chat.participant.isPassportVerified,
                        isDriverLicenseVerified = chat.participant.isDriverLicenseVerified,
                    )
                ChatListNameRow(
                    name = participantName,
                    verificationLabels = verificationLabels,
                    unreadCount = chat.unreadCount,
                )
                Row(
                    gap = 8.px,
                    alignItems = AlignItems.Center,
                    modifier = {
                        width(100.percent)
                        minWidth(0.px)
                        property("overflow", "hidden")
                    },
                ) {
                    Span({
                        style {
                            fontSize(14.px)
                            color(CSSColors.Gray600)
                            property("overflow", "hidden")
                            property("text-overflow", "ellipsis")
                            property("white-space", "nowrap")
                            property("min-width", "0")
                            flex(1)
                        }
                    }) {
                        // Plain text preview — links/wrapping in MessageTextWithDealsLink break ellipsis
                        Text(lastMessageText.ifBlank { "Нет сообщений" })
                    }
                    if (relativeTime.isNotBlank()) {
                        Span({
                            style {
                                fontSize(12.px)
                                color(CSSColors.Gray600)
                                property("flex-shrink", "0")
                                property("white-space", "nowrap")
                            }
                        }) {
                            Text(relativeTime)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatListNameRow(
    name: String,
    verificationLabels: List<String>,
    unreadCount: Int,
) {
    var isMobile by remember { mutableStateOf(window.innerWidth <= 768) }
    DisposableEffect(Unit) {
        val onResize: (Event) -> Unit = {
            isMobile = window.innerWidth <= 768
        }
        window.addEventListener("resize", onResize)
        onDispose { window.removeEventListener("resize", onResize) }
    }

    Div({
        style {
            display(DisplayStyle.Flex)
            flexDirection(FlexDirection.Row)
            alignItems(AlignItems.Center)
            gap(8.px)
            width(100.percent)
            minWidth(0.px)
            property("overflow", "hidden")
            property("box-sizing", "border-box")
        }
    }) {
        Span({
            style {
                fontSize(16.px)
                fontWeight("600")
                color(CSSColors.Black)
                property("overflow", "hidden")
                property("text-overflow", "ellipsis")
                property("white-space", "nowrap")
                property("min-width", "0")
                flex(1)
            }
        }) {
            Text(name)
        }
        if (verificationLabels.isNotEmpty()) {
            VerificationBadgeRow(
                labels = verificationLabels,
                compact = isMobile,
            )
        }
        if (unreadCount > 0) {
            Span({
                style {
                    padding(4.px, 8.px)
                    borderRadius(50.percent)
                    backgroundColor(CSSColors.Blue)
                    color(CSSColors.White)
                    fontSize(12.px)
                    fontWeight("600")
                    property("flex-shrink", "0")
                }
            }) {
                Text("$unreadCount")
            }
        }
    }
}

