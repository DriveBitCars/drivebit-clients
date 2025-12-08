package my.drivebit.repositories

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import my.drivebit.network.services.Photo
import my.drivebit.shared.storage.Storage

private const val IMAGE_URL = "images"

interface AvatarRepository {
    val avatarUrl: Flow<String>

    fun refresh()
}

internal class AvatarRepositoryImpl(
    private val photo: Photo,
    private val storage: Storage,
    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) : AvatarRepository {
    private val _avatarUrl = MutableStateFlow("$IMAGE_URL/menu/burger.svg")
    override val avatarUrl: Flow<String> = _avatarUrl.asStateFlow()

    init {
        refresh()
    }

    private suspend fun calculateAvatarUrl(): String =
        if (storage.isLogined()) {
            "$IMAGE_URL/menu/burger.svg"
        } else {
            runCatching {
                photo.getAvatar().url
            }.getOrElse {
                "$IMAGE_URL/menu/burger.svg"
            }
        }

    override fun refresh() {
        coroutineScope.launch {
            val newUrl = calculateAvatarUrl()
            if (_avatarUrl.value != newUrl) {
                _avatarUrl.update { newUrl }
            }
        }
    }
}
