package my.drivebit.viewmodels

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import my.drivebit.network.services.Chat
import my.drivebit.network.services.ChatListDto
import my.drivebit.utils.safeLaunchWithErrorHandler

interface ChatListViewModel {
    val chats: StateFlow<List<ChatListDto>>
    val isLoading: StateFlow<Boolean>
    val error: StateFlow<String?>

    fun loadChats(search: String? = null)

    fun refreshChats()
}

class ChatListViewModelImpl(
    private val chat: Chat,
    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) : ChatListViewModel {
    private val _chats = MutableStateFlow<List<ChatListDto>>(emptyList())
    override val chats: StateFlow<List<ChatListDto>> = _chats.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    override val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    override val error: StateFlow<String?> = _error.asStateFlow()

    override fun loadChats(search: String?) {
        if (_isLoading.value) return

        coroutineScope.safeLaunchWithErrorHandler(
            isLoading = { _isLoading.value },
            setLoading = { _isLoading.value = it },
            setError = { _error.value = it },
            errorHandler = { e ->
                ErrorHandler.extractErrorMessage(
                    exception = e,
                    defaultNetworkError = "Ошибка сети",
                    defaultGenericError = "Не удалось загрузить чаты",
                )
            },
        ) {
            val result = chat.getChats(limit = 50, offset = 0, search = search)
            _chats.value = result.chats ?: emptyList()
        }
    }

    override fun refreshChats() {
        coroutineScope.launch {
            runCatching {
                val result = chat.getChats(limit = 50, offset = 0)
                _chats.value = result.chats ?: emptyList()
                _error.value = null
            }
        }
    }
}
