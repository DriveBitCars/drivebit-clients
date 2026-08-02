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
import my.drivebit.components.ActionButton
import my.drivebit.shell.AppWithHeader
import my.drivebit.components.Column
import my.drivebit.components.Loader
import my.drivebit.components.MessageTextWithDealsLink
import my.drivebit.components.ParticipantAvatar
import my.drivebit.components.Row
import my.drivebit.components.TextError
import my.drivebit.components.ToolbarBackArrow
import my.drivebit.design.CSSColors
import my.drivebit.network.services.BookingDTO
import my.drivebit.network.services.MessageDto
import my.drivebit.network.services.canShowSignContractInChat
import my.drivebit.network.services.contractBookingIdForAction
import my.drivebit.network.services.contractDownloadPagePath
import my.drivebit.network.services.isLeaveReviewForCarAction
import my.drivebit.network.services.leaveReviewCarIdForAction
import my.drivebit.network.services.leaveReviewPagePath
import my.drivebit.network.services.payBookingIdForAction
import my.drivebit.network.services.prepaymentButtonLabel
import my.drivebit.network.services.renterFullOrBalanceAmountRub
import my.drivebit.network.services.renterFullOrBalancePaymentLabel
import my.drivebit.network.services.shouldShowLeaveReviewForRenter
import my.drivebit.analytics.reachYandexGoalPayAlreadyPaid
import my.drivebit.analytics.reachYandexGoalPayClick
import my.drivebit.analytics.reachYandexGoalPayFail
import my.drivebit.analytics.reachYandexGoalPayRedirect
import my.drivebit.utils.PaymentFunnelKind
import my.drivebit.utils.PaymentFunnelSource
import my.drivebit.utils.getUrlParameter
import my.drivebit.utils.mapIso8601ToTimeString
import my.drivebit.viewmodels.ButtonState
import my.drivebit.viewmodels.ChatDetailViewModel
import my.drivebit.viewmodels.ChatPayEffect
import my.drivebit.viewmodels.UnreadMessagesViewModel
import my.drivebit.viewmodels.VerificationLabels
import my.drivebit.viewmodels.createButtonViewModel
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
    val isPaying by viewModel.isPaying.collectAsState()
    val signActionInProgress by viewModel.signActionInProgress.collectAsState()
    val bookingPaymentById by viewModel.bookingPaymentById.collectAsState()
    var lastChatPayKind by remember { mutableStateOf(PaymentFunnelKind.Full) }

    LaunchedEffect(chatId) {
        viewModel.payEffects.collect { effect ->
            when (effect) {
                is ChatPayEffect.OpenCheckout -> {
                    reachYandexGoalPayRedirect(
                        source = PaymentFunnelSource.Chat,
                        kind = lastChatPayKind,
                        bookingId = effect.bookingId,
                    )
                    window.location.href = effect.url
                }
                is ChatPayEffect.ShowInfo -> {
                    if (effect.alreadyPaid) {
                        reachYandexGoalPayAlreadyPaid(
                            source = PaymentFunnelSource.Chat,
                            kind = lastChatPayKind,
                            bookingId = effect.bookingId,
                        )
                    } else {
                        reachYandexGoalPayFail(
                            source = PaymentFunnelSource.Chat,
                            kind = lastChatPayKind,
                            bookingId = effect.bookingId,
                            message = effect.text,
                        )
                    }
                    window.alert(effect.text)
                }
            }
        }
    }

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

    LaunchedEffect(Unit) {
        val styleId = "chat-panel-overflow-fix"
        val css =
            """
            #chat-panel,
            #chat-messages-scroll {
              overflow-x: hidden !important;
              scrollbar-width: none;
              -ms-overflow-style: none;
            }
            #chat-panel::-webkit-scrollbar,
            #chat-messages-scroll::-webkit-scrollbar {
              display: none !important;
              width: 0 !important;
              height: 0 !important;
            }
            """.trimIndent()
        val existing = document.getElementById(styleId)
        if (existing != null) {
            existing.textContent = css
        } else {
            val style = document.createElement("style")
            style.id = styleId
            style.textContent = css
            document.head?.appendChild(style)
        }
        document.getElementById("chat-messages-scroll-hide-bar")?.remove()
    }

    var messageText by remember { mutableStateOf("") }

    AppWithHeader {
        Column(
            modifier = {
                width(100.percent)
                maxWidth(100.percent)
                minWidth(0.px)
                property("box-sizing", "border-box")
                property("overflow-x", "hidden")
            },
        ) {
            ToolbarBackArrow(
                title = chatDetail?.participant?.name?.takeIf { it.isNotBlank() } ?: "Чат",
                onBackClick = { window.location.href = "/chats" },
                badges =
                    VerificationLabels.forUser(
                        isPassportVerified = chatDetail?.participant?.isPassportVerified == true,
                        isDriverLicenseVerified = chatDetail?.participant?.isDriverLicenseVerified == true,
                    ),
            )

            when {
                isLoading && messages.isEmpty() -> Loader()
                error != null -> TextError(error ?: "Ошибка")
                else -> {
                    Div({
                        attr("id", "chat-panel")
                        style {
                            display(DisplayStyle.Flex)
                            flexDirection(FlexDirection.Column)
                            width(100.percent)
                            maxWidth(100.percent)
                            minWidth(0.px)
                            property("box-sizing", "border-box")
                            property("height", "calc(100dvh - 160px)")
                            property("min-height", "420px")
                            property("overflow-x", "hidden")
                            property("overflow-y", "hidden")
                            backgroundColor(CSSColors.White)
                        }
                    }) {
                        Div({
                            attr("id", "chat-messages-scroll")
                            style {
                                flex(1)
                                minHeight(0.px)
                                minWidth(0.px)
                                width(100.percent)
                                maxWidth(100.percent)
                                property("box-sizing", "border-box")
                                property("overflow-x", "hidden")
                                property("overflow-y", "auto")
                                padding(16.px, 12.px)
                                property("scrollbar-width", "none")
                                property("-ms-overflow-style", "none")
                            }
                        }) {
                            Column(
                                gap = 10.px,
                                modifier = {
                                    width(100.percent)
                                    maxWidth(100.percent)
                                    minWidth(0.px)
                                    property("box-sizing", "border-box")
                                },
                            ) {
                                messages.forEach { message ->
                                    val bookingIdForPay = message.payBookingIdForAction()
                                    MessageBubble(
                                        message = message,
                                        participantId = chatDetail?.participant?.id,
                                        participantName = chatDetail?.participant?.name,
                                        participantAvatarUrl = chatDetail?.participant?.avatar,
                                        bookingForPay = bookingIdForPay?.let { bookingPaymentById[it] },
                                        bookingById = bookingPaymentById,
                                        isPaying = isPaying,
                                        signActionInProgress = signActionInProgress,
                                        onPrepayBooking = { bookingId ->
                                            lastChatPayKind = PaymentFunnelKind.Prepay
                                            reachYandexGoalPayClick(
                                                source = PaymentFunnelSource.Chat,
                                                kind = PaymentFunnelKind.Prepay,
                                                bookingId = bookingId,
                                            )
                                            val origin = window.location.origin
                                            viewModel.prepayBooking(
                                                bookingId = bookingId,
                                                returnUrl = "$origin/payment-success",
                                                failUrl = "$origin/payment-failure",
                                            )
                                        },
                                        onPayBooking = { bookingId ->
                                            lastChatPayKind = PaymentFunnelKind.Full
                                            reachYandexGoalPayClick(
                                                source = PaymentFunnelSource.Chat,
                                                kind = PaymentFunnelKind.Full,
                                                bookingId = bookingId,
                                            )
                                            val origin = window.location.origin
                                            viewModel.payBooking(
                                                bookingId = bookingId,
                                                returnUrl = "$origin/payment-success",
                                                failUrl = "$origin/payment-failure",
                                            )
                                        },
                                        onSignContract = { bookingId, counterpartyUserId ->
                                            viewModel.signContract(
                                                bookingId = bookingId,
                                                counterpartyUserId = counterpartyUserId,
                                            )
                                        },
                                    )
                                }
                            }
                        }

                        Div({
                            style {
                                display(DisplayStyle.Flex)
                                flexDirection(FlexDirection.Row)
                                alignItems(AlignItems.Center)
                                gap(10.px)
                                padding(12.px, 14.px)
                                width(100.percent)
                                maxWidth(100.percent)
                                minWidth(0.px)
                                flexShrink(0)
                                property("box-sizing", "border-box")
                                property("overflow-x", "hidden")
                                backgroundColor(CSSColors.White)
                                property("border-top", "1px solid ${CSSColors.Gray300}")
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
                                placeholder("Сообщение")
                                style {
                                    flex(1)
                                    minWidth(0.px)
                                    width(100.percent)
                                    property("box-sizing", "border-box")
                                    padding(12.px, 16.px)
                                    property("border", "1px solid ${CSSColors.Gray300}")
                                    borderRadius(22.px)
                                    fontSize(17.px)
                                    lineHeight("1.4")
                                    backgroundColor(CSSColors.White)
                                    property("outline", "none")
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
                                    flexShrink(0)
                                    padding(12.px, 18.px)
                                    backgroundColor(CSSColors.Blue)
                                    color(CSSColors.White)
                                    property("border", "none")
                                    borderRadius(22.px)
                                    cursor("pointer")
                                    fontSize(16.px)
                                    fontWeight("600")
                                    whiteSpace("nowrap")
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
    bookingForPay: BookingDTO? = null,
    bookingById: Map<String, BookingDTO> = emptyMap(),
    isPaying: Boolean = false,
    signActionInProgress: Set<String> = emptySet(),
    onPrepayBooking: (String) -> Unit = {},
    onPayBooking: (String) -> Unit = {},
    onSignContract: (bookingId: String, counterpartyUserId: String) -> Unit = { _, _ -> },
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
        val bookingIdForPay = message.payBookingIdForAction()
        val bookingIdForContract = message.contractBookingIdForAction()
        val reviewCarId = message.leaveReviewCarIdForAction(bookingById)
        val showReviewForCar = message.isLeaveReviewForCarAction()
        val showReviewForRenter = message.shouldShowLeaveReviewForRenter()
        val payButtonVm = createButtonViewModel()
        val contractButtonVm = createButtonViewModel()
        val signContractButtonVm = createButtonViewModel()
        val reviewCarButtonVm = createButtonViewModel()
        val reviewRenterButtonVm = createButtonViewModel()
        val bookingForContract = bookingIdForContract?.let { bookingById[it] }
        val showSignContract =
            bookingIdForContract != null &&
                participantId != null &&
                bookingForContract?.canShowSignContractInChat(participantId) == true
        val isSigningContract = bookingIdForContract != null && bookingIdForContract in signActionInProgress
        LaunchedEffect(isPaying) {
            payButtonVm.setState(if (isPaying) ButtonState.Loading else ButtonState.Enabled)
        }
        LaunchedEffect(isSigningContract) {
            signContractButtonVm.setState(
                if (isSigningContract) ButtonState.Loading else ButtonState.Enabled,
            )
        }
        LaunchedEffect(reviewCarId) {
            reviewCarButtonVm.setState(
                when {
                    reviewCarId != null -> ButtonState.Enabled
                    showReviewForCar -> ButtonState.Loading
                    else -> ButtonState.Disabled
                },
            )
        }
        Div({
            style {
                display(DisplayStyle.Flex)
                justifyContent(JustifyContent.Center)
                width(100.percent)
                maxWidth(100.percent)
                minWidth(0.px)
                property("box-sizing", "border-box")
                padding(4.px, 8.px)
            }
        }) {
            Div({
                style {
                    padding(10.px, 14.px)
                    backgroundColor(CSSColors.White)
                    borderRadius(14.px)
                    textAlign("center")
                    color(CSSColors.Gray600)
                    fontSize(16.px)
                    lineHeight("1.45")
                    width(100.percent)
                    maxWidth(100.percent)
                    property("box-sizing", "border-box")
                    property("overflow-wrap", "anywhere")
                    property("box-shadow", "0 1px 2px rgba(9, 5, 43, 0.06)")
                }
            }) {
                Column(
                    gap = 10.px,
                    modifier = {
                        width(100.percent)
                        alignItems(AlignItems.Center)
                    },
                ) {
                    MessageTextWithDealsLink(text = text.ifBlank { "Системное сообщение" })
                    if (bookingIdForPay != null) {
                        val fullPayLabel =
                            bookingForPay?.let { booking ->
                                "${booking.renterFullOrBalancePaymentLabel()} (${booking.renterFullOrBalanceAmountRub()} ₽)"
                            } ?: "Оплатить"
                        Column(gap = 8.px, modifier = { width(100.percent); maxWidth(280.px) }) {
                            if (bookingForPay?.canPayPrepayment == true) {
                                ActionButton(
                                    text = bookingForPay.prepaymentButtonLabel(),
                                    enabledColor = CSSColors.Blue,
                                    viewModel = payButtonVm,
                                    onClick = { onPrepayBooking(bookingIdForPay) },
                                )
                            }
                            ActionButton(
                                text = fullPayLabel,
                                enabledColor = CSSColors.Blue,
                                viewModel = payButtonVm,
                                onClick = { onPayBooking(bookingIdForPay) },
                            )
                        }
                    }
                    if (bookingIdForContract != null) {
                        Div({
                            style {
                                width(100.percent)
                                maxWidth(280.px)
                            }
                        }) {
                            ActionButton(
                                text = "Скачать договор",
                                enabledColor = CSSColors.Blue,
                                viewModel = contractButtonVm,
                                onClick = {
                                    window.location.href = contractDownloadPagePath(bookingIdForContract)
                                },
                            )
                        }
                    }
                    if (showSignContract) {
                        Div({
                            style {
                                width(100.percent)
                                maxWidth(280.px)
                            }
                        }) {
                            ActionButton(
                                text = if (isSigningContract) "Подписание..." else "Подписать договор",
                                enabledColor = CSSColors.Blue,
                                viewModel = signContractButtonVm,
                                onClick = {
                                    onSignContract(bookingIdForContract!!, participantId!!)
                                },
                            )
                        }
                    }
                    if (showReviewForCar) {
                        Div({
                            style {
                                width(100.percent)
                                maxWidth(280.px)
                            }
                        }) {
                            ActionButton(
                                text = if (reviewCarId != null) "Оставить отзыв" else "Загрузка…",
                                enabledColor = CSSColors.Blue,
                                viewModel = reviewCarButtonVm,
                                onClick = {
                                    reviewCarId?.let { carId ->
                                        window.location.href = leaveReviewPagePath(carId)
                                    }
                                },
                            )
                        }
                    }
                    if (showReviewForRenter) {
                        Div({
                            style {
                                width(100.percent)
                                maxWidth(280.px)
                            }
                        }) {
                            ActionButton(
                                text = "Оставить отзыв об арендаторе",
                                enabledColor = CSSColors.Blue,
                                viewModel = reviewRenterButtonVm,
                                onClick = {
                                    window.location.href = "/my-deals"
                                },
                            )
                        }
                    }
                }
            }
        }
        return
    }

    val showOpponentAvatar = !isOwnMessage && participantId != null
    val bubbleBg = if (isOwnMessage) rgb(232, 240, 255) else CSSColors.White
    val nameLabel = if (isOwnMessage) "" else senderName

    Div({
        style {
            display(DisplayStyle.Flex)
            flexDirection(FlexDirection.Row)
            alignItems(AlignItems.FlexEnd)
            justifyContent(if (isOwnMessage) JustifyContent.FlexEnd else JustifyContent.FlexStart)
            gap(8.px)
            width(100.percent)
            maxWidth(100.percent)
            minWidth(0.px)
            property("box-sizing", "border-box")
        }
    }) {
        if (showOpponentAvatar) {
            ParticipantAvatar(
                userId = participantId,
                name = participantName?.takeIf { it.isNotBlank() } ?: "Собеседник",
                initialAvatarUrl = participantAvatarUrl,
                size = 34.px,
            )
        }
        Div({
            style {
                padding(10.px, 14.px)
                backgroundColor(bubbleBg)
                if (isOwnMessage) {
                    borderRadius(18.px, 18.px, 4.px, 18.px)
                } else {
                    borderRadius(18.px, 18.px, 18.px, 4.px)
                    property("border", "1px solid ${CSSColors.Gray300}")
                }
                minWidth(0.px)
                property("max-width", if (showOpponentAvatar) "calc(100% - 42px)" else "85%")
                property("box-sizing", "border-box")
                property("overflow-wrap", "anywhere")
                property("box-shadow", "0 1px 2px rgba(9, 5, 43, 0.06)")
            }
        }) {
            Column(
                gap = 2.px,
                modifier = {
                    minWidth(0.px)
                    maxWidth(100.percent)
                    property("box-sizing", "border-box")
                },
            ) {
                if (nameLabel.isNotBlank()) {
                    Span({
                        style {
                            fontSize(13.px)
                            color(CSSColors.Blue)
                            fontWeight("600")
                            marginBottom(2.px)
                        }
                    }) {
                        Text(nameLabel)
                    }
                }
                Span({
                    style {
                        fontSize(18.px)
                        lineHeight("1.4")
                        color(CSSColors.Black)
                        property("word-break", "break-word")
                        property("overflow-wrap", "anywhere")
                    }
                }) {
                    MessageTextWithDealsLink(text = text)
                }
                Span({
                    style {
                        fontSize(12.px)
                        color(CSSColors.Gray600)
                        alignSelf(AlignSelf.FlexEnd)
                        marginTop(2.px)
                    }
                }) {
                    Text(timeStr)
                }
            }
        }
    }
}
