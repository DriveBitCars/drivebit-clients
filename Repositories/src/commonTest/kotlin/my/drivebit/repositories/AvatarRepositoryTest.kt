package my.drivebit.repositories

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import my.drivebit.network.services.AvatarResponse
import my.drivebit.network.services.Photo
import my.drivebit.shared.storage.Storage
import kotlin.test.Test
import kotlin.test.assertEquals

private const val IMAGE_URL = "images"

private class FakePhoto : Photo {
    var shouldThrow = false
    var avatarUrl: String = "https://example.com/avatar.jpg"

    override suspend fun getAvatar(): AvatarResponse {
        if (shouldThrow) {
            throw Exception("Network error")
        }
        return AvatarResponse(url = avatarUrl)
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

@OptIn(ExperimentalCoroutinesApi::class)
class AvatarRepositoryTest {
    @Test
    fun `avatarUrl should return default image when user is logged in`() =
        runTest {
            val storage = FakeStorage().apply { setLoggedIn(true) }
            val photo = FakePhoto()
            val repository = AvatarRepositoryImpl(photo, storage, this)

            advanceUntilIdle()
            val url = repository.avatarUrl.first()

            assertEquals("$IMAGE_URL/menu/burger.svg", url)
        }

    @Test
    fun `avatarUrl should return photo service URL when user is not logged in`() =
        runTest {
            val storage = FakeStorage().apply { setLoggedIn(false) }
            val photo = FakePhoto().apply { avatarUrl = "https://api.example.com/avatar.jpg" }
            val repository = AvatarRepositoryImpl(photo, storage, this)

            advanceUntilIdle()
            val url = repository.avatarUrl.first()

            assertEquals("https://api.example.com/avatar.jpg", url)
        }

    @Test
    fun `refresh should update avatarUrl when user logs in`() =
        runTest {
            val storage = FakeStorage().apply { setLoggedIn(false) }
            val photo = FakePhoto().apply { avatarUrl = "https://api.example.com/avatar.jpg" }
            val repository = AvatarRepositoryImpl(photo, storage, this)

            advanceUntilIdle()
            val initialUrl = repository.avatarUrl.first()
            assertEquals("https://api.example.com/avatar.jpg", initialUrl)

            storage.setLoggedIn(true)
            repository.refresh()
            advanceUntilIdle()

            val updatedUrl = repository.avatarUrl.first()
            assertEquals("$IMAGE_URL/menu/burger.svg", updatedUrl)
        }

    @Test
    fun `refresh should update avatarUrl when user logs out`() =
        runTest {
            val storage = FakeStorage().apply { setLoggedIn(true) }
            val photo = FakePhoto().apply { avatarUrl = "https://api.example.com/avatar.jpg" }
            val repository = AvatarRepositoryImpl(photo, storage, this)

            advanceUntilIdle()
            val initialUrl = repository.avatarUrl.first()
            assertEquals("$IMAGE_URL/menu/burger.svg", initialUrl)

            storage.setLoggedIn(false)
            repository.refresh()
            advanceUntilIdle()

            val updatedUrl = repository.avatarUrl.first()
            assertEquals("https://api.example.com/avatar.jpg", updatedUrl)
        }

    @Test
    fun `refresh should update avatarUrl when photo service URL changes`() =
        runTest {
            val storage = FakeStorage().apply { setLoggedIn(false) }
            val photo = FakePhoto().apply { avatarUrl = "https://api.example.com/avatar1.jpg" }
            val repository = AvatarRepositoryImpl(photo, storage, this)

            advanceUntilIdle()
            val initialUrl = repository.avatarUrl.first()
            assertEquals("https://api.example.com/avatar1.jpg", initialUrl)

            photo.avatarUrl = "https://api.example.com/avatar2.jpg"
            repository.refresh()
            advanceUntilIdle()

            val updatedUrl = repository.avatarUrl.first()
            assertEquals("https://api.example.com/avatar2.jpg", updatedUrl)
        }

    @Test
    fun `refresh should handle photo service errors gracefully`() =
        runTest {
            val storage = FakeStorage().apply { setLoggedIn(false) }
            val photo =
                FakePhoto().apply {
                    shouldThrow = true
                    avatarUrl = "https://api.example.com/avatar.jpg"
                }
            val repository = AvatarRepositoryImpl(photo, storage, this)

            advanceUntilIdle()
            val initialUrl = repository.avatarUrl.first()
            assertEquals("$IMAGE_URL/menu/burger.svg", initialUrl)

            photo.shouldThrow = false
            photo.avatarUrl = "https://api.example.com/new-avatar.jpg"
            repository.refresh()
            advanceUntilIdle()

            val updatedUrl = repository.avatarUrl.first()
            assertEquals("https://api.example.com/new-avatar.jpg", updatedUrl)
        }
}
