package my.drivebit.repositories

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNot
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
                
                // Для dev.drivebit.my и drivebit.my используем относительный путь (как для /api/)
                // Это позволяет nginx проксировать запросы через тот же домен
                if (shouldUseRelativeAvatarPath()) {
                    // Convert HTTP/HTTPS URL to relative path for nginx proxy
                    // http://213.171.27.185:9000/publicbct/avatars/... -> /publicbct/avatars/...
                    // https://213.171.27.185:9000/publicbct/avatars/... -> /publicbct/avatars/...
                    val relativePath = when {
                        apiUrl.startsWith("http://213.171.27.185:9000") -> {
                            apiUrl.removePrefix("http://213.171.27.185:9000")
                        }
                        apiUrl.startsWith("https://213.171.27.185:9000") -> {
                            apiUrl.removePrefix("https://213.171.27.185:9000")
                        }
                        apiUrl.startsWith("http://") || apiUrl.startsWith("https://") -> {
                            // Extract path from any HTTP/HTTPS URL
                            // Remove protocol and domain, keep path and query
                            val withoutProtocol = apiUrl.removePrefix("http://").removePrefix("https://")
                            val pathStart = withoutProtocol.indexOf('/')
                            if (pathStart >= 0) {
                                withoutProtocol.substring(pathStart)
                            } else {
                                "/"
                            }
                        }
                        else -> {
                            // Already a relative path or unknown format
                            apiUrl
                        }
                    }
                    // Ensure path starts with /
                    if (relativePath.isNotEmpty() && !relativePath.startsWith("/")) {
                        "/$relativePath"
                    } else {
                        relativePath
                    }
                } else {
                    // Для других доменов используем URL как есть
                    apiUrl
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
