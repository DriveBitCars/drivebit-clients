package my.drivebit.mobile.screens.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.delay
import my.drivebit.mobile.screens.main.LeaveReviewScreen
import my.drivebit.network.services.BookingDTO
import my.drivebit.network.services.MessageDto
import my.drivebit.network.services.canShowSignContractInChat
import my.drivebit.network.services.contractBookingIdForAction
import my.drivebit.network.services.contractDownloadPageUrl
import my.drivebit.network.services.isLeaveReviewForCarAction
import my.drivebit.network.services.leaveReviewCarIdForAction
import my.drivebit.network.services.payBookingIdForAction
import my.drivebit.network.services.prepaymentButtonLabel
import my.drivebit.network.services.renterFullOrBalanceAmountRub
import my.drivebit.network.services.renterFullOrBalancePaymentLabel
import my.drivebit.network.services.shouldShowLeaveReviewForRenter
import my.drivebit.ui.components.ApplicationTopBar
import my.drivebit.ui.components.Loader
import my.drivebit.utils.mapIso8601ToTimeString
import my.drivebit.viewmodels.ChatDetailViewModel
import my.drivebit.viewmodels.ChatPayEffect
import my.drivebit.viewmodels.VerificationLabels
import org.koin.compose.currentKoinScope
import org.koin.core.parameter.parametersOf

private const val MOBILE_PAYMENT_SUCCESS_URL = "https://drivebit.ru/payment-success"
private const val MOBILE_PAYMENT_FAIL_URL = "https://drivebit.ru/payment-failure"

data class ChatScreen(
    val chatId: String,
) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val koinScope = currentKoinScope()
        val viewModel: ChatDetailViewModel =
            remember(chatId) {
                koinScope.get<ChatDetailViewModel>(parameters = { parametersOf(chatId) })
            }
        val chatDetail by viewModel.chatDetail.collectAsState()
        val messages by viewModel.messages.collectAsState()
        val isLoading by viewModel.isLoading.collectAsState()
        val error by viewModel.error.collectAsState()
        val isPaying by viewModel.isPaying.collectAsState()
        val signActionInProgress by viewModel.signActionInProgress.collectAsState()
        val bookingPaymentById by viewModel.bookingPaymentById.collectAsState()
        var messageText by remember { mutableStateOf("") }
        var payInfo by remember { mutableStateOf<String?>(null) }
        val uriHandler = LocalUriHandler.current

        LaunchedEffect(chatId) {
            viewModel.payEffects.collect { effect ->
                when (effect) {
                    is ChatPayEffect.OpenCheckout -> uriHandler.openUri(effect.url)
                    is ChatPayEffect.ShowInfo -> {
                        payInfo = effect.text
                    }
                }
            }
        }

        LaunchedEffect(payInfo) {
            val msg = payInfo
            if (msg != null) {
                delay(6_000)
                payInfo = null
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

        Scaffold(
            topBar = {
                ApplicationTopBar(
                    title = chatDetail?.participant?.name?.takeIf { it.isNotBlank() } ?: "Чат",
                    onBackClick = { navigator.pop() },
                    badges =
                        VerificationLabels.forUser(
                            isPassportVerified = chatDetail?.participant?.isPassportVerified == true,
                            isDriverLicenseVerified = chatDetail?.participant?.isDriverLicenseVerified == true,
                        ),
                )
            },
        ) { innerPadding ->
            when {
                isLoading && messages.isEmpty() -> Loader()
                error != null -> Text("Ошибка: $error", modifier = Modifier.padding(innerPadding))
                else ->
                    Column(
                        modifier = Modifier.padding(innerPadding).fillMaxSize(),
                    ) {
                        payInfo?.let { info ->
                            Text(
                                text = info,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            )
                        }
                        LazyColumn(
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                            reverseLayout = true,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding =
                                androidx.compose.foundation.layout
                                    .PaddingValues(16.dp),
                        ) {
                            items(messages.reversed()) { message ->
                                val bookingIdForPay = message.payBookingIdForAction()
                                MessageBubble(
                                    message = message,
                                    participantId = chatDetail?.participant?.id,
                                    bookingForPay = bookingIdForPay?.let { bookingPaymentById[it] },
                                    bookingById = bookingPaymentById,
                                    isPaying = isPaying,
                                    signActionInProgress = signActionInProgress,
                                    onPrepayBooking = { bookingId ->
                                        viewModel.prepayBooking(
                                            bookingId = bookingId,
                                            returnUrl = MOBILE_PAYMENT_SUCCESS_URL,
                                            failUrl = MOBILE_PAYMENT_FAIL_URL,
                                        )
                                    },
                                    onPayBooking = { bookingId ->
                                        viewModel.payBooking(
                                            bookingId = bookingId,
                                            returnUrl = MOBILE_PAYMENT_SUCCESS_URL,
                                            failUrl = MOBILE_PAYMENT_FAIL_URL,
                                        )
                                    },
                                    onLeaveReviewForCar = { carId ->
                                        navigator.push(LeaveReviewScreen(carId = carId))
                                    },
                                    onLeaveReviewForRenter = {
                                        uriHandler.openUri("https://drivebit.ru/my-deals")
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
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            val focusManager = LocalFocusManager.current
                            OutlinedTextField(
                                value = messageText,
                                onValueChange = { messageText = it },
                                placeholder = { Text("Введите сообщение...") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                                keyboardActions =
                                    KeyboardActions(
                                        onSend = {
                                            if (messageText.isNotBlank()) {
                                                viewModel.sendMessage(messageText.trim())
                                                messageText = ""
                                                focusManager.clearFocus()
                                            }
                                        },
                                    ),
                            )
                            androidx.compose.material3.Button(
                                onClick = {
                                    if (messageText.isNotBlank()) {
                                        viewModel.sendMessage(messageText.trim())
                                        messageText = ""
                                    }
                                },
                            ) {
                                Text("Отправить")
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
    bookingForPay: BookingDTO? = null,
    bookingById: Map<String, BookingDTO> = emptyMap(),
    isPaying: Boolean = false,
    signActionInProgress: Set<String> = emptySet(),
    onPrepayBooking: (String) -> Unit = {},
    onPayBooking: (String) -> Unit = {},
    onLeaveReviewForCar: (String) -> Unit = {},
    onLeaveReviewForRenter: () -> Unit = {},
    onSignContract: (bookingId: String, counterpartyUserId: String) -> Unit = { _, _ -> },
) {
    val senderId = message.sender?.id
    val isOwnMessage = participantId != null && senderId != null && senderId != participantId
    val displayName =
        when {
            message.isSystemMessage -> null
            isOwnMessage -> "Вы"
            else -> message.sender?.name?.takeIf { it.isNotBlank() }
        }
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        if (!message.isSystemMessage) {
            displayName?.let { name ->
                Text(
                    text = name,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Text(
                text = message.text ?: "",
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = runCatching { mapIso8601ToTimeString(message.createdAt) }.getOrElse { "" },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            val bookingIdForPay = message.payBookingIdForAction()
            val bookingIdForContract = message.contractBookingIdForAction()
            val reviewCarId = message.leaveReviewCarIdForAction(bookingById)
            val showReviewForCar = message.isLeaveReviewForCarAction()
            val showReviewForRenter = message.shouldShowLeaveReviewForRenter()
            val bookingForContract = bookingIdForContract?.let { bookingById[it] }
            val showSignContract =
                bookingIdForContract != null &&
                    participantId != null &&
                    bookingForContract?.canShowSignContractInChat(participantId) == true
            val isSigningContract = bookingIdForContract != null && bookingIdForContract in signActionInProgress
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = message.text ?: "Системное сообщение",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (bookingIdForPay != null) {
                    val fullPayLabel =
                        bookingForPay?.let { booking ->
                            "${booking.renterFullOrBalancePaymentLabel()} (${booking.renterFullOrBalanceAmountRub()} ₽)"
                        } ?: if (isPaying) "Загрузка…" else "Оплатить"
                    if (bookingForPay?.canPayPrepayment == true) {
                        Button(
                            onClick = { onPrepayBooking(bookingIdForPay) },
                            enabled = !isPaying,
                            modifier = Modifier.padding(top = 8.dp),
                        ) {
                            Text(if (isPaying) "Загрузка…" else bookingForPay.prepaymentButtonLabel())
                        }
                    }
                    Button(
                        onClick = { onPayBooking(bookingIdForPay) },
                        enabled = !isPaying,
                        modifier = Modifier.padding(top = 8.dp),
                    ) {
                        Text(fullPayLabel)
                    }
                }
                if (bookingIdForContract != null) {
                    val uriHandler = LocalUriHandler.current
                    Button(
                        onClick = { uriHandler.openUri(contractDownloadPageUrl(bookingIdForContract)) },
                        modifier = Modifier.padding(top = 8.dp),
                    ) {
                        Text("Скачать договор")
                    }
                }
                if (showSignContract) {
                    Button(
                        onClick = { onSignContract(bookingIdForContract!!, participantId!!) },
                        enabled = !isSigningContract,
                        modifier = Modifier.padding(top = 8.dp),
                    ) {
                        Text(if (isSigningContract) "Подписание..." else "Подписать договор")
                    }
                }
                if (showReviewForCar) {
                    Button(
                        onClick = { reviewCarId?.let(onLeaveReviewForCar) },
                        enabled = reviewCarId != null,
                        modifier = Modifier.padding(top = 8.dp),
                    ) {
                        Text(if (reviewCarId != null) "Оставить отзыв" else "Загрузка…")
                    }
                }
                if (showReviewForRenter) {
                    Button(
                        onClick = onLeaveReviewForRenter,
                        modifier = Modifier.padding(top = 8.dp),
                    ) {
                        Text("Оставить отзыв об арендаторе")
                    }
                }
            }
        }
    }
}
