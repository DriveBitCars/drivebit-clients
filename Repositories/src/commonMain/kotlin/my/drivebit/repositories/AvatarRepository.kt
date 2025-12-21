package my.drivebit.repositories

import kotlinx.coroutines.flow.Flow
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

    init {
        println("🏗️ [AvatarRepository] Instance created (hashCode: ${hashCode()})")
    }

    override fun clearCache() {
        cachedRepository.clearCache()
    }

    override suspend fun refresh() {
        clearCache()
        calculateAvatarUrl()
    }

    internal suspend fun calculateAvatarUrl(): String =
        if (storage.isLogined()) {
            runCatching {
                val apiUrl = photo.getAvatar().url

                val path =
                    when {
                        apiUrl.startsWith("http://155.212.170.94:9000") -> {
                            apiUrl.removePrefix("http://155.212.170.94:9000")
                        }
                        apiUrl.startsWith("https://155.212.170.94:9000") -> {
                            apiUrl.removePrefix("https://155.212.170.94:9000")
                        }
                        apiUrl.startsWith("http://") || apiUrl.startsWith("https://") -> {
                            val withoutProtocol = apiUrl.removePrefix("http://").removePrefix("https://")
                            val pathStart = withoutProtocol.indexOf('/')
                            if (pathStart >= 0) {
                                withoutProtocol.substring(pathStart)
                            } else {
                                "/"
                            }
                        }
                        else -> {
                            if (apiUrl.startsWith("/")) apiUrl else "/$apiUrl"
                        }
                    }

                val avatarPath =
                    if (path.startsWith("/publicbct/avatars/")) {
                        path.removePrefix("/publicbct/avatars/")
                    } else if (path.startsWith("/publicbct/avatars")) {
                        path.removePrefix("/publicbct/avatars")
                    } else {
                        path.substringAfterLast('/')
                    }

                "https://drivebit.my/avatar/$avatarPath"
            }.getOrElse {
                DEFAULT_AVATAR_PATH
            }
        } else {
            DEFAULT_AVATAR_PATH
        }
}
