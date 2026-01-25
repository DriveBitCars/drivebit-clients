package my.drivebit.repositories

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import my.drivebit.network.services.AvatarResponse
import my.drivebit.network.services.CarPhotoResponse
import my.drivebit.network.services.Photo
import my.drivebit.shared.storage.Storage
import kotlin.test.Test
import kotlin.test.assertEquals

private class FakePhoto : Photo {
    var shouldThrow = false
    var avatarUrl: String = "https://example.com/avatar.jpg"

    override suspend fun getAvatar(): AvatarResponse {
        if (shouldThrow) {
            throw Exception("Network error")
        }
        return AvatarResponse(url = avatarUrl)
    }

    override suspend fun uploadAvatar(
        fileBytes: ByteArray,
        fileName: String,
        contentType: String,
    ): AvatarResponse {
        if (shouldThrow) {
            throw Exception("Network error")
        }
        return AvatarResponse(url = avatarUrl)
    }

    override suspend fun getCarPhotos(carId: String): List<CarPhotoResponse> = emptyList()

    override suspend fun uploadCarPhotos(
        carId: String,
        fileBytesList: List<ByteArray>,
        fileNames: List<String>,
        contentTypes: List<String>,
    ): List<CarPhotoResponse> = emptyList()

    override suspend fun deleteCarPhoto(photoId: Int) {
        // No-op for testing
    }
}

private class FakeStorage : Storage {
    private var token: String? = null
    private var refreshToken: String? = null

    override fun getToken(): String? = token

    override fun getRefreshToken(): String? = refreshToken

    override fun saveToken(token: String) {
        this.token = token
    }

    override fun saveRefreshToken(refreshToken: String) {
        this.refreshToken = refreshToken
    }

    override fun logout() {
        token = null
        refreshToken = null
    }

    override fun isLogined(): Boolean = token != null

    override fun putString(
        key: String,
        value: String,
    ) {}

    override fun getString(
        key: String,
        defaultValue: String,
    ): String = defaultValue

    override fun contains(key: String): Boolean = false

    override fun remove(key: String) {}

    fun setLoggedIn(loggedIn: Boolean) {
        if (loggedIn) {
            token = "test-token"
            refreshToken = "test-refresh-token"
        } else {
            token = null
            refreshToken = null
        }
    }
}

private class TestCachedRepository(
    private val photo: Photo,
    private val storage: Storage,
) : CachedRepository<String> {
    private var cachedValue: String? = null

    override fun get(): Flow<String> =
        flow {
            if (cachedValue == null) {
                val tempRepository =
                    AvatarRepositoryImpl(
                        photo,
                        storage,
                        object : CachedRepository<String> {
                            override fun get(): Flow<String> = flow { emit("") }

                            override fun clearCache() {}
                        },
                    )
                cachedValue = tempRepository.calculateAvatarUrl()
            }
            emit(cachedValue!!)
        }

    override fun clearCache() {
        cachedValue = null
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class AvatarRepositoryTest {
    @Test
    fun `calculateAvatarUrl should preserve publicbct path from MinIO URL`() =
        runTest {
            val storage = FakeStorage().apply { setLoggedIn(true) }
            val photo =
                FakePhoto().apply {
                    avatarUrl =
                        "http://155.212.170.94:9000/publicbct/avatars/bf0ad237-da64-4542-8779-a803d6c7d3cc/283b4525-9c35-4267-b0c2-e325e6534d03_avatar.jpg"
                }
            val cachedRepository = TestCachedRepository(photo, storage)
            val repository = AvatarRepositoryImpl(photo, storage, cachedRepository)

            advanceUntilIdle()
            val url = repository.avatarUrl.first()

            assertEquals(
                "/publicbct/avatars/bf0ad237-da64-4542-8779-a803d6c7d3cc/283b4525-9c35-4267-b0c2-e325e6534d03_avatar.jpg",
                url,
            )
        }
}
