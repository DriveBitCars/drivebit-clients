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
import my.drivebit.network.services.Chat
import my.drivebit.network.services.ChatDetailDto
import my.drivebit.network.services.MessageDto
import my.drivebit.network.services.PayBookingResult
import my.drivebit.network.services.Payment
import my.drivebit.network.services.SendMessageRequest
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
    val isLoading: StateFlow<Boolean>
    val error: StateFlow<String?>
    val isSending: StateFlow<Boolean>

    val isPaying: StateFlow<Boolean>

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
}

class ChatDetailViewModelImpl(
    private val chat: Chat,
    private val payment: Payment,
    private val chatId: String,
    private val participantAvatarCache: ParticipantAvatarCache,
    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) : ChatDetailViewModel {
    private val _chatDetail = MutableStateFlow<ChatDetailDto?>(null)
    override val chatDetail: StateFlow<ChatDetailDto?> = _chatDetail.asStateFlow()

    private val _messages = MutableStateFlow<List<MessageDto>>(emptyList())
    override val messages: StateFlow<List<MessageDto>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    override val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    override val error: StateFlow<String?> = _error.asStateFlow()

    private val _isSending = MutableStateFlow(false)
    override val isSending: StateFlow<Boolean> = _isSending.asStateFlow()

    private val _isPaying = MutableStateFlow(false)
    override val isPaying: StateFlow<Boolean> = _isPaying.asStateFlow()

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
            _messages.value = (result.messages ?: emptyList()).sortedBy { it.createdAt }
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
        if (bookingId.isBlank() || _isPaying.value) return
        coroutineScope.launch {
            _isPaying.value = true
            try {
                when (val result = payment.registerBookingPayment(bookingId, returnUrl, failUrl)) {
                    is PayBookingResult.Redirect ->
                        _payEffects.emit(ChatPayEffect.OpenCheckout(result.url))
                    is PayBookingResult.AlreadyPaid -> {
                        val text =
                            result.message?.takeIf { it.isNotBlank() }
                                ?: "Оплата уже выполнена"
                        _payEffects.emit(ChatPayEffect.ShowInfo(text))
                        runCatching {
                            val refreshed = chat.getMessages(chatId, limit = 50)
                            _messages.value =
                                (refreshed.messages ?: emptyList()).sortedBy { it.createdAt }
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
