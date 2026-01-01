package my.drivebit.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import my.drivebit.network.services.Photo
import my.drivebit.shared.storage.Storage
import my.drivebit.utils.DEFAULT_AVATAR_PATH

interface AvatarRepository {
    val avatarUrl: Flow<String>

    fun clearCache()

    suspend fun refresh()
}

internal class AvatarRepositoryImpl(
    private val photo: Photo,
    private val storage: Storage,
    private val cachedRepository: CachedRepository<String>,
) : AvatarRepository,
    CachedRepository<String> by cachedRepository {
    override val avatarUrl: Flow<String> = cachedRepository.get()

    override fun clearCache() {
        cachedRepository.clearCache()
    }

    override suspend fun refresh() {
        clearCache()
        cachedRepository.get().first()
    }

    internal suspend fun calculateAvatarUrl(): String =
        if (storage.isLogined()) {
            runCatching {
                val apiUrl = photo.getAvatar().url
                val path = extractPathFromApiUrl(apiUrl)
                val avatarPath = extractAvatarFileName(path)
                "https://drivebit.my/avatar/$avatarPath"
            }.getOrElse {
                DEFAULT_AVATAR_PATH
            }
        } else {
            DEFAULT_AVATAR_PATH
        }

    private fun extractPathFromApiUrl(apiUrl: String): String =
        when {
            apiUrl.startsWith("http://155.212.170.94:9000") -> apiUrl.removePrefix("http://155.212.170.94:9000")
            apiUrl.startsWith("https://155.212.170.94:9000") -> apiUrl.removePrefix("https://155.212.170.94:9000")
            apiUrl.startsWith("http://") || apiUrl.startsWith("https://") -> {
                val withoutProtocol = apiUrl.removePrefix("http://").removePrefix("https://")
                val pathStart = withoutProtocol.indexOf('/')
                if (pathStart >= 0) withoutProtocol.substring(pathStart) else "/"
            }
            else -> if (apiUrl.startsWith("/")) apiUrl else "/$apiUrl"
        }

    private fun extractAvatarFileName(path: String): String =
        when {
            path.startsWith("/publicbct/avatars/") -> path.removePrefix("/publicbct/avatars/")
            path.startsWith("/publicbct/avatars") -> path.removePrefix("/publicbct/avatars")
            else -> path.substringAfterLast('/')
        }
}
