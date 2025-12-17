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

                // Извлекаем путь из URL API и преобразуем в /avatar/...
                val path = when {
                    apiUrl.startsWith("http://155.212.170.94:9000") -> {
                        apiUrl.removePrefix("http://155.212.170.94:9000")
                    }
                    apiUrl.startsWith("https://155.212.170.94:9000") -> {
                        apiUrl.removePrefix("https://155.212.170.94:9000")
                    }
                    apiUrl.startsWith("http://") || apiUrl.startsWith("https://") -> {
                        // Extract path from any HTTP/HTTPS URL
                        val withoutProtocol = apiUrl.removePrefix("http://").removePrefix("https://")
                        val pathStart = withoutProtocol.indexOf('/')
                        if (pathStart >= 0) {
                            withoutProtocol.substring(pathStart)
                        } else {
                            "/"
                        }
                    }
                    else -> {
                        // Already a relative path
                        if (apiUrl.startsWith("/")) apiUrl else "/$apiUrl"
                    }
                }
                
                // Преобразуем /publicbct/avatars/... в /avatar/...
                val avatarPath = if (path.startsWith("/publicbct/avatars/")) {
                    path.removePrefix("/publicbct/avatars/")
                } else if (path.startsWith("/publicbct/avatars")) {
                    path.removePrefix("/publicbct/avatars")
                } else {
                    // Если путь не начинается с /publicbct/avatars/, извлекаем только имя файла
                    path.substringAfterLast('/')
                }
                
                "/avatar/$avatarPath"
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
