package my.drivebit.repositories

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import my.drivebit.network.services.Photo
import my.drivebit.shared.storage.Storage
import my.drivebit.utils.DEFAULT_AVATAR_PATH
import my.drivebit.utils.extractPathFromApiUrl

interface AvatarRepository {
    val avatarUrl: Flow<String>

    fun clearCache()

    suspend fun refresh()
}

internal class AvatarRepositoryImpl(
    private val photo: Photo,
    private val storage: Storage,
    private val cachedRepository: CachedRepository<String>,
    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) : AvatarRepository,
    CachedRepository<String> by cachedRepository {
    private val _avatarUrlState = MutableStateFlow<String?>(null)
    internal val avatarUrlState get() = _avatarUrlState

    init {
        coroutineScope.launch {
            cachedRepository.get().collect { url ->
                avatarUrlState.value = url
            }
        }
    }

    override val avatarUrl: Flow<String> =
        kotlinx.coroutines.flow.flow {
            if (avatarUrlState.value == null) {
                val initialValue = cachedRepository.get().first()
                avatarUrlState.value = initialValue
            }
            avatarUrlState.collect { value ->
                value?.let { emit(it) }
            }
        }

    override fun clearCache() {
        cachedRepository.clearCache()
    }

    override suspend fun refresh() {
        clearCache()
        val newUrl = cachedRepository.get().first()
        avatarUrlState.value = newUrl
    }

    internal suspend fun calculateAvatarUrl(): String =
        if (storage.isLogined()) {
            runCatching {
                val apiUrl = photo.getAvatar().url
                extractPathFromApiUrl(apiUrl)
            }.getOrElse {
                DEFAULT_AVATAR_PATH
            }
        } else {
            DEFAULT_AVATAR_PATH
        }
}
