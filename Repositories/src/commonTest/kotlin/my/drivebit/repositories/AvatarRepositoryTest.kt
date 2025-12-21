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
import my.drivebit.utils.DEFAULT_AVATAR_PATH
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
    fun `avatarUrl should return photo service URL when user is logged in`() =
        runTest {
            val storage = FakeStorage().apply { setLoggedIn(true) }
            val photo = FakePhoto().apply { avatarUrl = "https://api.example.com/avatar.jpg" }
            val cachedRepository = TestCachedRepository(photo, storage)
            val repository = AvatarRepositoryImpl(photo, storage, cachedRepository)

            advanceUntilIdle()
            val url = repository.avatarUrl.first()

            assertEquals("https://drivebit.my/avatar/avatar.jpg", url)
        }

    @Test
    fun `avatarUrl should return default image when user is not logged in`() =
        runTest {
            val storage = FakeStorage().apply { setLoggedIn(false) }
            val photo = FakePhoto()
            val cachedRepository = TestCachedRepository(photo, storage)
            val repository = AvatarRepositoryImpl(photo, storage, cachedRepository)

            advanceUntilIdle()
            val url = repository.avatarUrl.first()

            assertEquals(DEFAULT_AVATAR_PATH, url)
        }

    @Test
    fun `refresh should update avatarUrl when user logs in`() =
        runTest {
            val storage = FakeStorage().apply { setLoggedIn(false) }
            val photo = FakePhoto().apply { avatarUrl = "https://api.example.com/avatar.jpg" }
            val cachedRepository = TestCachedRepository(photo, storage)
            val repository = AvatarRepositoryImpl(photo, storage, cachedRepository)

            advanceUntilIdle()
            val initialUrl = repository.avatarUrl.first()
            assertEquals(DEFAULT_AVATAR_PATH, initialUrl)

            storage.setLoggedIn(true)
            repository.refresh()
            advanceUntilIdle()

            val updatedUrl = repository.avatarUrl.first()
            assertEquals("https://drivebit.my/avatar/avatar.jpg", updatedUrl)
        }

    @Test
    fun `refresh should update avatarUrl when user logs out`() =
        runTest {
            val storage = FakeStorage().apply { setLoggedIn(true) }
            val photo = FakePhoto().apply { avatarUrl = "https://api.example.com/avatar.jpg" }
            val cachedRepository = TestCachedRepository(photo, storage)
            val repository = AvatarRepositoryImpl(photo, storage, cachedRepository)

            advanceUntilIdle()
            val initialUrl = repository.avatarUrl.first()
            assertEquals("https://drivebit.my/avatar/avatar.jpg", initialUrl)

            storage.setLoggedIn(false)
            repository.refresh()
            advanceUntilIdle()

            val updatedUrl = repository.avatarUrl.first()
            assertEquals(DEFAULT_AVATAR_PATH, updatedUrl)
        }

    @Test
    fun `refresh should update avatarUrl when photo service URL changes`() =
        runTest {
            val storage = FakeStorage().apply { setLoggedIn(true) }
            val photo = FakePhoto().apply { avatarUrl = "https://api.example.com/avatar1.jpg" }
            val cachedRepository = TestCachedRepository(photo, storage)
            val repository = AvatarRepositoryImpl(photo, storage, cachedRepository)

            advanceUntilIdle()
            val initialUrl = repository.avatarUrl.first()
            assertEquals("https://drivebit.my/avatar/avatar1.jpg", initialUrl)

            photo.avatarUrl = "https://api.example.com/avatar2.jpg"
            repository.refresh()
            advanceUntilIdle()

            val updatedUrl = repository.avatarUrl.first()
            assertEquals("https://drivebit.my/avatar/avatar2.jpg", updatedUrl)
        }

    @Test
    fun `refresh should handle photo service errors gracefully`() =
        runTest {
            val storage = FakeStorage().apply { setLoggedIn(true) }
            val photo =
                FakePhoto().apply {
                    shouldThrow = true
                    avatarUrl = "https://api.example.com/avatar.jpg"
                }
            val cachedRepository = TestCachedRepository(photo, storage)
            val repository = AvatarRepositoryImpl(photo, storage, cachedRepository)

            advanceUntilIdle()
            val initialUrl = repository.avatarUrl.first()
            assertEquals(DEFAULT_AVATAR_PATH, initialUrl)

            photo.shouldThrow = false
            photo.avatarUrl = "https://api.example.com/new-avatar.jpg"
            repository.refresh()
            advanceUntilIdle()

            val updatedUrl = repository.avatarUrl.first()
            assertEquals("https://drivebit.my/avatar/new-avatar.jpg", updatedUrl)
        }
}
