package my.drivebit.viewmodels

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import my.drivebit.network.services.Chat
import my.drivebit.shared.storage.Storage

interface UnreadMessagesViewModel {
    val hasUnread: StateFlow<Boolean>
}

private const val POLLING_INTERVAL_MS = 30_000L

class UnreadMessagesViewModelImpl(
    private val chat: Chat,
    private val storage: Storage,
    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) : UnreadMessagesViewModel {
    private val _hasUnread = MutableStateFlow(false)
    override val hasUnread: StateFlow<Boolean> = _hasUnread.asStateFlow()

    init {
        coroutineScope.launch {
            while (true) {
                refreshSync()
                delay(POLLING_INTERVAL_MS)
            }
        }
    }

    private suspend fun refreshSync() {
        if (!storage.isLogined()) {
            _hasUnread.value = false
            return
        }
        runCatching {
            val response = chat.hasUnread()
            _hasUnread.value = response.hasUnread
        }.onFailure {
            _hasUnread.value = false
        }
    }
}
