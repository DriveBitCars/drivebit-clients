package my.drivebit.viewmodels

import my.drivebit.shared.storage.Storage
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MockStorageForButter : Storage {
    private var isLoggedIn = false
    private var token: String? = null

    override fun isLogined(): Boolean = isLoggedIn

    override fun saveToken(token: String) {
        this.token = token
        this.isLoggedIn = token.isNotEmpty()
    }

    override fun getToken(): String? = token

    fun setLoggedIn(loggedIn: Boolean) {
        this.isLoggedIn = loggedIn
    }
}

class ButterViewModelTest {
    private val mockStorage = MockStorageForButter()

    @Test
    fun `state should return non-empty list`() {
        val viewModel = ButterViewModelImpl(mockStorage)

        assertFalse(viewModel.state.isEmpty(), "State list should not be empty")
        assertTrue(viewModel.state.isNotEmpty(), "State list should contain at least one element")
    }
}
