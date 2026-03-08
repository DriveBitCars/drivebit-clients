package my.drivebit.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import my.drivebit.network.services.Photo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

interface ParticipantAvatarCache {
    val avatarUrlByUserId: StateFlow<Map<String, String>>

    fun fetchIfNeeded(userId: String, initialAvatarUrl: String? = null)
}

class ParticipantAvatarCacheImpl(
    private val photo: Photo,
    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) : ParticipantAvatarCache {
    private val _avatarUrlByUserId = MutableStateFlow<Map<String, String>>(emptyMap())
    override val avatarUrlByUserId: StateFlow<Map<String, String>> = _avatarUrlByUserId.asStateFlow()

    override fun fetchIfNeeded(userId: String, initialAvatarUrl: String?) {
        if (userId.isBlank()) return
        if (_avatarUrlByUserId.value[userId] != null) return
        if (initialAvatarUrl?.isNotBlank() == true) {
            _avatarUrlByUserId.update { it + (userId to initialAvatarUrl) }
            return
        }
        coroutineScope.launch {
            runCatching {
                photo.getAvatarByUserId(userId)?.url
            }.getOrNull()?.let { url ->
                if (url.isNotBlank()) {
                    _avatarUrlByUserId.update { it + (userId to url) }
                }
            }
        }
    }
}
