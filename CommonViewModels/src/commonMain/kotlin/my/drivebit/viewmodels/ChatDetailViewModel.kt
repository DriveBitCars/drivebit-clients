package my.drivebit.viewmodels

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import my.drivebit.network.services.Booking
import my.drivebit.network.services.BookingCheckoutKind
import my.drivebit.network.services.BookingDTO
import my.drivebit.network.services.Chat
import my.drivebit.network.services.ChatDetailDto
import my.drivebit.network.services.MessageDto
import my.drivebit.network.services.PayBookingResult
import my.drivebit.network.services.Payment
import my.drivebit.network.services.SendMessageRequest
import my.drivebit.network.services.checkoutBooking
import my.drivebit.network.services.contractBookingIdForAction
import my.drivebit.network.services.leaveReviewBookingIdForAction
import my.drivebit.network.services.payBookingIdForAction
import my.drivebit.network.services.SignContractChatRole
import my.drivebit.network.services.signContractChatRole
import my.drivebit.repositories.ParticipantAvatarCache
import my.drivebit.utils.safeLaunchWithErrorHandler

sealed interface ChatPayEffect {
    data class OpenCheckout(
        val url: String,
    ) : ChatPayEffect

    data class ShowInfo(
        val text: String,
    ) : ChatPayEffect
}

interface ChatDetailViewModel {
    val chatDetail: StateFlow<ChatDetailDto?>
    val messages: StateFlow<List<MessageDto>>
    val bookingPaymentById: StateFlow<Map<String, BookingDTO>>
    val isLoading: StateFlow<Boolean>
    val error: StateFlow<String?>
    val isSending: StateFlow<Boolean>

    val isPaying: StateFlow<Boolean>

    val signActionInProgress: StateFlow<Set<String>>

    val payEffects: SharedFlow<ChatPayEffect>

    fun loadChat()

    fun loadMessages()

    fun loadMoreMessages(before: String)

    fun sendMessage(text: String)

    fun payBooking(
        bookingId: String,
        returnUrl: String,
        failUrl: String,
    )

    fun prepayBooking(
        bookingId: String,
        returnUrl: String,
        failUrl: String,
    )

    fun signContract(
        bookingId: String,
        counterpartyUserId: String,
    )
}

class ChatDetailViewModelImpl(
    private val chat: Chat,
    private val booking: Booking,
    private val payment: Payment,
    private val chatId: String,
    private val participantAvatarCache: ParticipantAvatarCache,
    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) : ChatDetailViewModel {
    private val _chatDetail = MutableStateFlow<ChatDetailDto?>(null)
    override val chatDetail: StateFlow<ChatDetailDto?> = _chatDetail.asStateFlow()

    private val _messages = MutableStateFlow<List<MessageDto>>(emptyList())
    override val messages: StateFlow<List<MessageDto>> = _messages.asStateFlow()

    private val _bookingPaymentById = MutableStateFlow<Map<String, BookingDTO>>(emptyMap())
    override val bookingPaymentById: StateFlow<Map<String, BookingDTO>> = _bookingPaymentById.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    override val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    override val error: StateFlow<String?> = _error.asStateFlow()

    private val _isSending = MutableStateFlow(false)
    override val isSending: StateFlow<Boolean> = _isSending.asStateFlow()

    private val _isPaying = MutableStateFlow(false)
    override val isPaying: StateFlow<Boolean> = _isPaying.asStateFlow()

    private val _signActionInProgress = MutableStateFlow<Set<String>>(emptySet())
    override val signActionInProgress: StateFlow<Set<String>> = _signActionInProgress.asStateFlow()

    private val _payEffects = MutableSharedFlow<ChatPayEffect>(extraBufferCapacity = 1)
    override val payEffects: SharedFlow<ChatPayEffect> = _payEffects.asSharedFlow()

    override fun loadChat() {
        coroutineScope.safeLaunchWithErrorHandler(
            isLoading = { _isLoading.value },
            setLoading = { _isLoading.value = it },
            setError = { _error.value = it },
            errorHandler = { e ->
                ErrorHandler.extractErrorMessage(
                    exception = e,
                    defaultNetworkError = "Ошибка сети",
                    defaultGenericError = "Не удалось загрузить чат",
                )
            },
        ) {
            val detail = chat.getChat(chatId)
            _chatDetail.value = detail
            detail?.participant?.let { participant ->
                participantAvatarCache.fetchIfNeeded(participant.id, participant.avatar)
            }
        }
    }

    override fun loadMessages() {
        if (_isLoading.value) return

        coroutineScope.safeLaunchWithErrorHandler(
            isLoading = { _isLoading.value },
            setLoading = { _isLoading.value = it },
            setError = { _error.value = it },
            errorHandler = { e ->
                ErrorHandler.extractErrorMessage(
                    exception = e,
                    defaultNetworkError = "Ошибка сети",
                    defaultGenericError = "Не удалось загрузить сообщения",
                )
            },
        ) {
            val result = chat.getMessages(chatId, limit = 50)
            val loaded = (result.messages ?: emptyList()).sortedBy { it.createdAt }
            _messages.value = loaded
            refreshBookingsForMessageActions(loaded)
        }
    }

    override fun signContract(
        bookingId: String,
        counterpartyUserId: String,
    ) {
        if (bookingId.isBlank() || bookingId in _signActionInProgress.value) return
        val dto = _bookingPaymentById.value[bookingId] ?: return
        val role = dto.signContractChatRole(counterpartyUserId) ?: return

        coroutineScope.safeLaunchWithErrorHandler(
            isLoading = { false },
            setLoading = { },
            setError = { message ->
                coroutineScope.launch {
                    _payEffects.emit(ChatPayEffect.ShowInfo(message ?: "Не удалось подписать договор"))
                }
            },
            errorHandler = { e ->
                ErrorHandler.extractErrorMessage(
                    exception = e,
                    defaultNetworkError = "Ошибка сети",
                    defaultGenericError = "Не удалось подписать договор",
                )
            },
        ) {
            _signActionInProgress.update { it + bookingId }
            try {
                val updated =
                    when (role) {
                        SignContractChatRole.Owner -> booking.signContractAsOwner(bookingId)
                        SignContractChatRole.Renter -> booking.signContractAsRenter(bookingId)
                    }
                _bookingPaymentById.update { current -> current + (bookingId to updated) }
            } finally {
                _signActionInProgress.update { it - bookingId }
            }
        }
    }

    override fun loadMoreMessages(before: String) {
        coroutineScope.launch {
            runCatching {
                val result = chat.getMessages(chatId, limit = 20, before = before)
                val newMessages = result.messages ?: emptyList()
                _messages.update { current ->
                    (newMessages + current).distinctBy { it.id }.sortedBy { it.createdAt }
                }
            }
        }
    }

    override fun sendMessage(text: String) {
        if (text.isBlank() || _isSending.value) return

        coroutineScope.safeLaunchWithErrorHandler(
            isLoading = { _isSending.value },
            setLoading = { _isSending.value = it },
            setError = { _error.value = it },
            errorHandler = { e ->
                ErrorHandler.extractErrorMessage(
                    exception = e,
                    defaultNetworkError = "Ошибка сети",
                    defaultGenericError = "Не удалось отправить сообщение",
                )
            },
        ) {
            val message = chat.sendMessage(SendMessageRequest(chatId = chatId, text = text))
            _messages.update { it + message }
        }
    }

    override fun payBooking(
        bookingId: String,
        returnUrl: String,
        failUrl: String,
    ) {
        checkoutBooking(
            bookingId = bookingId,
            kind = BookingCheckoutKind.FullOrBalance,
            returnUrl = returnUrl,
            failUrl = failUrl,
        )
    }

    override fun prepayBooking(
        bookingId: String,
        returnUrl: String,
        failUrl: String,
    ) {
        checkoutBooking(
            bookingId = bookingId,
            kind = BookingCheckoutKind.Prepayment,
            returnUrl = returnUrl,
            failUrl = failUrl,
        )
    }

    private fun refreshBookingsForMessageActions(messages: List<MessageDto>) {
        val bookingIds =
            messages
                .flatMap { message ->
                    listOfNotNull(
                        message.payBookingIdForAction(),
                        message.leaveReviewBookingIdForAction(),
                        message.contractBookingIdForAction(),
                    )
                }.distinct()
        if (bookingIds.isEmpty()) return

        coroutineScope.launch {
            bookingIds.forEach { bookingId ->
                runCatching { booking.getById(bookingId) }
                    .getOrNull()
                    ?.let { dto ->
                        _bookingPaymentById.update { current -> current + (bookingId to dto) }
                    }
            }
        }
    }

    private fun checkoutBooking(
        bookingId: String,
        kind: BookingCheckoutKind,
        returnUrl: String,
        failUrl: String,
    ) {
        if (bookingId.isBlank() || _isPaying.value) return
        coroutineScope.launch {
            _isPaying.value = true
            try {
                when (val result = payment.checkoutBooking(bookingId, kind, returnUrl, failUrl)) {
                    is PayBookingResult.Redirect ->
                        _payEffects.emit(ChatPayEffect.OpenCheckout(result.url))
                    is PayBookingResult.AlreadyPaid -> {
                        val text =
                            result.message?.takeIf { it.isNotBlank() }
                                ?: "Оплата уже выполнена"
                        _payEffects.emit(ChatPayEffect.ShowInfo(text))
                        runCatching {
                            val refreshed = chat.getMessages(chatId, limit = 50)
                            val loaded = (refreshed.messages ?: emptyList()).sortedBy { it.createdAt }
                            _messages.value = loaded
                            refreshBookingsForMessageActions(loaded)
                        }
                    }
                    is PayBookingResult.Failed ->
                        _payEffects.emit(ChatPayEffect.ShowInfo(result.message))
                }
            } finally {
                _isPaying.value = false
            }
        }
    }
}
