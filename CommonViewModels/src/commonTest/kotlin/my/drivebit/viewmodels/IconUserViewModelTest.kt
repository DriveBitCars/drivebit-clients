package my.drivebit.viewmodels

import my.drivebit.shared.storage.Storage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MockStorageForIconUser : Storage {
    private var isLoggedIn = false
    private var token: String? = null
    private var refreshToken: String? = null

    override fun isLogined(): Boolean = isLoggedIn

    override fun saveToken(token: String) {
        this.token = token
        this.isLoggedIn = token.isNotEmpty()
    }

    override fun getToken(): String? = token

    override fun saveRefreshToken(refreshToken: String) {
        this.refreshToken = refreshToken
    }

    override fun getRefreshToken(): String? = refreshToken

    override fun logout() {
        token = null
        refreshToken = null
        isLoggedIn = false
    }

    fun setLoggedIn(loggedIn: Boolean) {
        this.isLoggedIn = loggedIn
    }
}

class IconUserViewModelTest {
    private val mockStorage = MockStorageForIconUser()

    @Test
    fun `userIconUrl should return user svg when user is not logged in`() {
        mockStorage.setLoggedIn(false)
        val viewModel = IconUserViewModel(mockStorage)

        assertTrue { viewModel.userIconUrl.startsWith(imageUrl) }
    }

    @Test
    fun `userIconUrl should return user svg when user is logged in`() {
        mockStorage.setLoggedIn(true)
        val viewModel = IconUserViewModel(mockStorage)

        assertTrue { viewModel.userIconUrl.startsWith(imageUrl) }
    }

    @Test
    fun `userIconUrl should be consistent regardless of login status`() {
        val viewModel = IconUserViewModel(mockStorage)

        mockStorage.setLoggedIn(false)
        val urlWhenNotLoggedIn = viewModel.userIconUrl

        mockStorage.setLoggedIn(true)
        val urlWhenLoggedIn = viewModel.userIconUrl

        assertEquals(urlWhenNotLoggedIn, urlWhenLoggedIn)
    }
}
