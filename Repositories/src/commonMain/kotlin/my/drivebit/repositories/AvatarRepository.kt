package my.drivebit.repositories

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
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
    private val _avatarUrl = MutableStateFlow<String?>(null)
    override val avatarUrl: Flow<String> = _avatarUrl.asStateFlow().filterNotNull()

    init {
        refresh()
    }

    private suspend fun calculateAvatarUrl(): String =
        if (storage.isLogined()) {
            runCatching {
                val apiUrl = photo.getAvatar().url

                // Преобразуем URL в формат для nginx проксирования через drivebit.my
                // http://213.171.27.185:9000/publicbct/avatars/... -> https://drivebit.my/publicbct/avatars/...
                // https://213.171.27.185:9000/publicbct/avatars/... -> https://drivebit.my/publicbct/avatars/...
                // Это работает как для dev.drivebit.my, так и для drivebit.my (как и API запросы)
                when {
                    apiUrl.startsWith("http://213.171.27.185:9000") -> {
                        val path = apiUrl.removePrefix("http://213.171.27.185:9000")
                        "https://drivebit.my$path"
                    }
                    apiUrl.startsWith("https://213.171.27.185:9000") -> {
                        val path = apiUrl.removePrefix("https://213.171.27.185:9000")
                        "https://drivebit.my$path"
                    }
                    apiUrl.startsWith("http://") || apiUrl.startsWith("https://") -> {
                        // Extract path from any HTTP/HTTPS URL and use drivebit.my domain
                        val withoutProtocol = apiUrl.removePrefix("http://").removePrefix("https://")
                        val pathStart = withoutProtocol.indexOf('/')
                        val path =
                            if (pathStart >= 0) {
                                withoutProtocol.substring(pathStart)
                            } else {
                                "/"
                            }
                        "https://drivebit.my$path"
                    }
                    else -> {
                        // Already a relative path - convert to full URL
                        val path = if (apiUrl.startsWith("/")) apiUrl else "/$apiUrl"
                        "https://drivebit.my$path"
                    }
                }
            }.getOrElse {
                DEFAULT_AVATAR_PATH
            }
        } else {
            DEFAULT_AVATAR_PATH
        }

    override fun refresh() {
        coroutineScope.launch {
            _avatarUrl.update { calculateAvatarUrl() }
        }
    }
}
