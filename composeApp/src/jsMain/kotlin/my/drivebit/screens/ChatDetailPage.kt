package my.drivebit.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.delay
import my.drivebit.components.AppWithHeader
import my.drivebit.components.Column
import my.drivebit.components.Loader
import my.drivebit.components.MessageTextWithDealsLink
import my.drivebit.components.ParticipantAvatar
import my.drivebit.components.Row
import my.drivebit.components.TextError
import my.drivebit.components.ToolbarBackArrow
import my.drivebit.design.CSSColors
import my.drivebit.network.services.MessageDto
import my.drivebit.utils.getUrlParameter
import my.drivebit.utils.mapIso8601ToTimeString
import my.drivebit.viewmodels.ChatDetailViewModel
import my.drivebit.viewmodels.UnreadMessagesViewModel
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.attributes.disabled
import org.jetbrains.compose.web.attributes.placeholder
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Input
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import org.koin.compose.currentKoinScope
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf

@Composable
fun ChatDetailPage() {
    val chatId = getUrlParameter("id")
    val unreadMessagesViewModel: UnreadMessagesViewModel = koinInject()
    val hasUnread by unreadMessagesViewModel.hasUnread.collectAsState()

    if (chatId.isBlank()) {
        AppWithHeader {
            Div({
                style {
                    width(100.percent)
                    padding(20.px)
                }
            }) {
                TextError("Не указан ID чата")
            }
        }
        return
    }

    val koinScope = currentKoinScope()
    val viewModel: ChatDetailViewModel =
        remember(chatId) {
            koinScope.get<ChatDetailViewModel>(parameters = { parametersOf(chatId) })
        }
    val chatDetail by viewModel.chatDetail.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val isSending by viewModel.isSending.collectAsState()

    LaunchedEffect(chatId) {
        viewModel.loadChat()
        viewModel.loadMessages()
    }

    LaunchedEffect(chatId) {
        while (true) {
            delay(10_000)
            viewModel.loadMessages()
        }
    }

    LaunchedEffect(hasUnread) {
        if (hasUnread) {
            viewModel.loadMessages()
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            delay(100)
            (document.getElementById("chat-messages-scroll") as? org.w3c.dom.HTMLElement)?.let { el ->
                el.scrollTop = el.scrollHeight.toDouble()
            }
        }
    }

    var messageText by remember { mutableStateOf("") }

    AppWithHeader {
        Column(modifier = { width(100.percent) }) {
            ToolbarBackArrow(
                title = chatDetail?.participant?.name?.takeIf { it.isNotBlank() } ?: "Чат",
                onBackClick = { window.location.href = "/chats" },
            )

            when {
                isLoading && messages.isEmpty() -> Loader()
                error != null -> TextError(error ?: "Ошибка")
                else -> {
                    Div({
                        style {
                            display(DisplayStyle.Flex)
                            flexDirection(FlexDirection.Column)
                            flex(1)
                            maxHeight(70.vh)
                            property("overflow", "hidden")
                        }
                    }) {
                        Div({
                            attr("id", "chat-messages-scroll")
                            style {
                                flex(1)
                                property("overflow-y", "auto")
                                padding(16.px)
                            }
                        }) {
                            Column(gap = 12.px, modifier = { width(100.percent) }) {
                                messages.forEach { message ->
                                    MessageBubble(
                                        message = message,
                                        participantId = chatDetail?.participant?.id,
                                        participantName = chatDetail?.participant?.name,
                                        participantAvatarUrl = chatDetail?.participant?.avatar,
                                    )
                                }
                            }
                        }

                        Div({
                            style {
                                display(DisplayStyle.Flex)
                                flexDirection(FlexDirection.Row)
                                alignItems(AlignItems.Center)
                                padding(16.px)
                                width(100.percent)
                                property("box-sizing", "border-box")
                            }
                        }) {
                            Input(InputType.Text) {
                                value(messageText)
                                onInput { messageText = it.target.value }
                                onKeyDown { event ->
                                    if (event.key == "Enter") {
                                        event.preventDefault()
                                        if (messageText.isNotBlank()) {
                                            viewModel.sendMessage(messageText.trim())
                                            messageText = ""
                                        }
                                    }
                                }
                                placeholder("Введите сообщение...")
                                style {
                                    flex(1)
                                    padding(12.px)
                                    property("border", "1px solid ${CSSColors.Gray300}")
                                    borderRadius(8.px)
                                    fontSize(14.px)
                                }
                            }
                            org.jetbrains.compose.web.dom.Button({
                                if (isSending) disabled()
                                onClick {
                                    if (messageText.isNotBlank()) {
                                        viewModel.sendMessage(messageText.trim())
                                        messageText = ""
                                    }
                                }
                                style {
                                    marginLeft(8.px)
                                    padding(12.px, 20.px)
                                    backgroundColor(CSSColors.Blue)
                                    color(CSSColors.White)
                                    property("border", "none")
                                    borderRadius(8.px)
                                    cursor("pointer")
                                    fontSize(14.px)
                                    fontWeight("600")
                                }
                            }) {
                                Text(if (isSending) "..." else "Отправить")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(
    message: MessageDto,
    participantId: String? = null,
    participantName: String? = null,
    participantAvatarUrl: String? = null,
) {
    val isSystemMessage = message.isSystemMessage
    val senderId = message.sender?.id
    val isOwnMessage = participantId != null && senderId != null && senderId != participantId
    val senderName =
        when {
            isSystemMessage -> ""
            isOwnMessage -> "Вы"
            else -> message.sender?.name?.takeIf { it.isNotBlank() } ?: ""
        }
    val text = message.text?.takeIf { it.isNotBlank() } ?: ""
    val timeStr = runCatching { mapIso8601ToTimeString(message.createdAt) }.getOrElse { "" }

    if (isSystemMessage) {
        Div({
            style {
                padding(8.px)
                textAlign("center")
                color(CSSColors.Gray600)
                fontSize(13.px)
            }
        }) {
            MessageTextWithDealsLink(text = text.ifBlank { "Системное сообщение" })
        }
        return
    }

    val showOpponentAvatar = !isOwnMessage && participantId != null

    Div({
        style {
            display(DisplayStyle.Flex)
            flexDirection(FlexDirection.Row)
            alignItems(AlignItems.FlexStart)
            gap(12.px)
            property("max-width", "100%")
        }
    }) {
        if (showOpponentAvatar) {
            ParticipantAvatar(
                userId = participantId,
                name = participantName?.takeIf { it.isNotBlank() } ?: "Собеседник",
                initialAvatarUrl = participantAvatarUrl,
                size = 40.px,
            )
        }
        Div({
            style {
                padding(12.px)
                borderRadius(12.px)
                property("max-width", "80%")
            }
        }) {
            Column(gap = 4.px) {
                if (senderName.isNotBlank()) {
                    Span({
                        style {
                            fontSize(12.px)
                            color(CSSColors.Gray600)
                            fontWeight("600")
                        }
                    }) {
                        Text(senderName)
                    }
                }
                Span({
                    style {
                        fontSize(14.px)
                        color(CSSColors.Black)
                    }
                }) {
                    MessageTextWithDealsLink(text = text)
                }
                Span({
                    style {
                        fontSize(11.px)
                        color(CSSColors.Gray600)
                    }
                }) {
                    Text(timeStr)
                }
            }
        }
    }
}
