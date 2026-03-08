package my.drivebit.viewmodels

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import my.drivebit.network.services.Chat
import my.drivebit.network.services.ChatDetailDto
import my.drivebit.repositories.ParticipantAvatarCache
import my.drivebit.network.services.MessageDto
import my.drivebit.network.services.SendMessageRequest
import my.drivebit.utils.safeLaunchWithErrorHandler

interface ChatDetailViewModel {
    val chatDetail: StateFlow<ChatDetailDto?>
    val messages: StateFlow<List<MessageDto>>
    val isLoading: StateFlow<Boolean>
    val error: StateFlow<String?>
    val isSending: StateFlow<Boolean>

    fun loadChat()

    fun loadMessages()

    fun loadMoreMessages(before: String)

    fun sendMessage(text: String)
}

class ChatDetailViewModelImpl(
    private val chat: Chat,
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
}
