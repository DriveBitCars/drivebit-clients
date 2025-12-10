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
import my.drivebit.utils.DEFAULT_AVATAR_PATH

interface AvatarRepository {
    val avatarUrl: Flow<String>

    /**
     * Обновляет URL аватара.
     * Если пользователь авторизован, получает URL из сервиса Photo.
     * Если пользователь не авторизован, возвращает дефолтный аватар (user.svg).
     * При ошибке получения URL из сервиса также возвращает дефолтный аватар.
     * Метод автоматически проверяет, изменился ли URL, и обновляет Flow только при необходимости.
     */
    fun refresh()
}

internal class AvatarRepositoryImpl(
    private val photo: Photo,
    private val storage: Storage,
    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) : AvatarRepository {
    private val _avatarUrl = MutableStateFlow(DEFAULT_AVATAR_PATH)
    override val avatarUrl: Flow<String> = _avatarUrl.asStateFlow()

    init {
        refresh()
    }

    private suspend fun calculateAvatarUrl(): String =
        if (storage.isLogined()) {
            runCatching {
                photo.getAvatar().url
            }.getOrElse {
                DEFAULT_AVATAR_PATH
            }
        } else {
            DEFAULT_AVATAR_PATH
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
