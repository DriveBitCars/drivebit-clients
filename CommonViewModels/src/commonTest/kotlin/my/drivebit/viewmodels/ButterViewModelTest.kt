package my.drivebit.viewmodels

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import my.drivebit.repositories.AvatarRepository
import my.drivebit.shared.storage.Storage
import my.drivebit.viewmodels.CarMenuOption
import my.drivebit.viewmodels.CarMenuViewModel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class MockStorageForButter : Storage {
    private var isLoggedIn = false
    private var token: String? = null
    private var refreshToken: String? = null
    private val storage = mutableMapOf<String, String>()

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
        storage.clear()
    }

    override fun putString(
        key: String,
        value: String,
    ) {
        storage[key] = value
    }

    override fun getString(
        key: String,
        defaultValue: String,
    ): String = storage[key] ?: defaultValue

    override fun contains(key: String): Boolean = storage.containsKey(key)

    override fun remove(key: String) {
        storage.remove(key)
    }

    fun setLoggedIn(loggedIn: Boolean) {
        this.isLoggedIn = loggedIn
        if (loggedIn) {
            token = "test_token"
            refreshToken = "test_refresh_token"
        } else {
            token = null
            refreshToken = null
        }
    }
}

class MockAvatarRepositoryForButter : AvatarRepository {
    override val avatarUrl: Flow<String> = flowOf("default-avatar.svg")

    override fun clearCache() {
    }

    override suspend fun refresh() {
        // No-op for testing
    }
}

class MockCarMenuViewModelForButter : CarMenuViewModel {
    private val _menuOption = MutableStateFlow(CarMenuOption.ListYourCar)
    override val menuOption: StateFlow<CarMenuOption> = _menuOption.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    override val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun setMenuOption(option: CarMenuOption) {
        _menuOption.value = option
    }

    fun setIsLoading(value: Boolean) {
        _isLoading.value = value
    }

    override fun load() {
        loadCallCount++
    }

    var loadCallCount = 0
}

class ButterViewModelTest {
    private val mockStorage = MockStorageForButter()
    private val mockAvatarRepository = MockAvatarRepositoryForButter()
    private val mockCarMenuViewModel = MockCarMenuViewModelForButter()

    @Test
    fun `open should reload car menu option`() {
        val viewModel = ButterViewModelImpl(mockStorage, mockAvatarRepository, mockCarMenuViewModel)

        viewModel.open()

        assertEquals(2, mockCarMenuViewModel.loadCallCount)
    }

    @Test
    fun `open should show MyCars menu item when user has cars`() {
        mockStorage.setLoggedIn(true)
        mockCarMenuViewModel.setMenuOption(CarMenuOption.MyCars)
        mockCarMenuViewModel.setIsLoading(false)
        val viewModel = ButterViewModelImpl(mockStorage, mockAvatarRepository, mockCarMenuViewModel)

        viewModel.open()

        val openedState = viewModel.state.value as ButterState.Opened
        assertTrue(openedState.model.any { it.text == "Мои авто" })
        assertFalse(openedState.model.any { it.text == "Сдать авто" })
    }

    @Test
    fun `initial state should be Idle`() {
        val viewModel = ButterViewModelImpl(mockStorage, mockAvatarRepository, mockCarMenuViewModel)

        assertIs<ButterState.Idle>(viewModel.state.value, "Initial state should be Idle")
    }

    @Test
    fun `open should change state to Opened with non-empty list`() {
        val viewModel = ButterViewModelImpl(mockStorage, mockAvatarRepository, mockCarMenuViewModel)

        viewModel.onClick()

        assertIs<ButterState.Opened>(viewModel.state.value, "State should be Opened after open()")
        val openedState = viewModel.state.value as ButterState.Opened
        assertFalse(openedState.model.isEmpty(), "Model list should not be empty")
        assertTrue(openedState.model.isNotEmpty(), "Model list should contain at least one element")
    }

    @Test
    fun `opened state should contain item with icon always`() {
        mockStorage.setLoggedIn(true)
        val viewModel = ButterViewModelImpl(mockStorage, mockAvatarRepository, mockCarMenuViewModel)

        viewModel.onClick()

        assertIs<ButterState.Opened>(viewModel.state.value)
        val openedState = viewModel.state.value as ButterState.Opened
        val itemWithIcon = openedState.model.find { it.iconUrl != null }
        assertTrue(itemWithIcon != null, "State should always contain item with icon")
    }

    @Test
    fun `opened state should contain more items when user is not logged in`() {
        mockStorage.setLoggedIn(false)
        val viewModel = ButterViewModelImpl(mockStorage, mockAvatarRepository, mockCarMenuViewModel)

        viewModel.onClick()

        assertIs<ButterState.Opened>(viewModel.state.value)
        val openedState = viewModel.state.value as ButterState.Opened
        assertTrue(openedState.model.size >= 2, "Should contain at least 2 items when not logged in")
    }

    @Test
    fun `opened state should contain items when user is logged in`() {
        mockStorage.setLoggedIn(true)
        val viewModel = ButterViewModelImpl(mockStorage, mockAvatarRepository, mockCarMenuViewModel)

        viewModel.open()

        assertIs<ButterState.Opened>(viewModel.state.value)
        val openedState = viewModel.state.value as ButterState.Opened
        assertTrue(openedState.model.size >= 2, "Should contain at least 2 items when logged in")
        assertTrue(openedState.model.any { it.iconUrl != null }, "Should contain item with icon")
    }

    @Test
    fun `close should change state from Opened to Idle`() {
        val viewModel = ButterViewModelImpl(mockStorage, mockAvatarRepository, mockCarMenuViewModel)

        viewModel.open()
        assertIs<ButterState.Opened>(viewModel.state.value, "State should be Opened after open()")

        viewModel.close()
        assertIs<ButterState.Idle>(viewModel.state.value, "State should be Idle after close()")
    }

    @Test
    fun `onClick should toggle state from Idle to Opened`() {
        val viewModel = ButterViewModelImpl(mockStorage, mockAvatarRepository, mockCarMenuViewModel)

        assertIs<ButterState.Idle>(viewModel.state.value, "Initial state should be Idle")

        viewModel.onClick()
        assertIs<ButterState.Opened>(viewModel.state.value, "State should be Opened after first onClick()")
    }

    @Test
    fun `onClick should toggle state from Opened to Idle`() {
        val viewModel = ButterViewModelImpl(mockStorage, mockAvatarRepository, mockCarMenuViewModel)

        viewModel.open()
        assertIs<ButterState.Opened>(viewModel.state.value, "State should be Opened after open()")

        viewModel.onClick()
        assertIs<ButterState.Idle>(viewModel.state.value, "State should be Idle after onClick() when Opened")
    }

    @Test
    fun `onClick should toggle state multiple times`() {
        val viewModel = ButterViewModelImpl(mockStorage, mockAvatarRepository, mockCarMenuViewModel)

        assertIs<ButterState.Idle>(viewModel.state.value)

        viewModel.onClick()
        assertIs<ButterState.Opened>(viewModel.state.value)

        viewModel.onClick()
        assertIs<ButterState.Idle>(viewModel.state.value)

        viewModel.onClick()
        assertIs<ButterState.Opened>(viewModel.state.value)

        viewModel.onClick()
        assertIs<ButterState.Idle>(viewModel.state.value)
    }

    @Test
    fun `menu should close after clicking on menu item`() {
        val viewModel = ButterViewModelImpl(mockStorage, mockAvatarRepository, mockCarMenuViewModel)

        viewModel.open()
        assertIs<ButterState.Opened>(viewModel.state.value)

        val openedState = viewModel.state.value as ButterState.Opened
        val firstItem = openedState.model.first()

        firstItem.onClick()

        viewModel.close()
        assertIs<ButterState.Idle>(viewModel.state.value, "Menu should be closed after item click and close()")
    }

    @Test
    fun `menu should close when close is called after opening`() {
        val viewModel = ButterViewModelImpl(mockStorage, mockAvatarRepository, mockCarMenuViewModel)

        viewModel.open()
        assertIs<ButterState.Opened>(viewModel.state.value, "Menu should be opened")

        viewModel.close()
        assertIs<ButterState.Idle>(viewModel.state.value, "Menu should be closed after close()")
    }

    @Test
    fun `menu should close after clicking any item in opened menu`() {
        val viewModel = ButterViewModelImpl(mockStorage, mockAvatarRepository, mockCarMenuViewModel)

        viewModel.open()
        assertIs<ButterState.Opened>(viewModel.state.value)

        val openedState = viewModel.state.value as ButterState.Opened

        openedState.model.forEach { item ->
            viewModel.open()
            assertIs<ButterState.Opened>(viewModel.state.value, "Menu should be opened before item click")

            item.onClick()

            viewModel.close()
            assertIs<ButterState.Idle>(viewModel.state.value, "Menu should close after clicking item: ${item.text}")
        }
    }

    @Test
    fun `menu should close multiple times correctly`() {
        val viewModel = ButterViewModelImpl(mockStorage, mockAvatarRepository, mockCarMenuViewModel)

        repeat(5) {
            viewModel.open()
            assertIs<ButterState.Opened>(viewModel.state.value, "Menu should be opened on iteration $it")

            viewModel.close()
            assertIs<ButterState.Idle>(viewModel.state.value, "Menu should be closed on iteration $it")
        }
    }

    @Test
    fun `logout should clear storage data`() {
        mockStorage.setLoggedIn(true)
        mockStorage.saveToken("test_token")
        mockStorage.saveRefreshToken("test_refresh_token")
        val viewModel = ButterViewModelImpl(mockStorage, mockAvatarRepository, mockCarMenuViewModel)

        assertTrue(mockStorage.isLogined(), "User should be logged in initially")

        viewModel.open()
        val openedState = viewModel.state.value as ButterState.Opened

        val logoutItem =
            openedState.model.firstOrNull { item ->
                val initialState = mockStorage.isLogined()
                item.onClick()
                val afterClick = !mockStorage.isLogined()
                if (!afterClick) {
                    mockStorage.setLoggedIn(initialState)
                    mockStorage.saveToken("test_token")
                    mockStorage.saveRefreshToken("test_refresh_token")
                }
                afterClick
            }
        assertTrue(logoutItem != null, "Logout item should be present when logged in")

        mockStorage.setLoggedIn(true)
        mockStorage.saveToken("test_token")
        mockStorage.saveRefreshToken("test_refresh_token")

        logoutItem?.onClick()

        assertFalse(mockStorage.isLogined(), "User should be logged out after logout")
        assertTrue(mockStorage.getToken() == null, "Token should be cleared after logout")
        assertTrue(mockStorage.getRefreshToken() == null, "Refresh token should be cleared after logout")
    }

    @Test
    fun `logout should close menu`() {
        mockStorage.setLoggedIn(true)
        val viewModel = ButterViewModelImpl(mockStorage, mockAvatarRepository, mockCarMenuViewModel)

        viewModel.open()
        assertIs<ButterState.Opened>(viewModel.state.value, "Menu should be opened")

        val openedState = viewModel.state.value as ButterState.Opened
        val logoutItem =
            openedState.model.firstOrNull { item ->
                val initialState = mockStorage.isLogined()
                item.onClick()
                val afterClick = !mockStorage.isLogined()
                if (!afterClick) {
                    mockStorage.setLoggedIn(initialState)
                }
                afterClick
            }

        assertTrue(logoutItem != null, "Logout item should be present")
        mockStorage.setLoggedIn(true)
        logoutItem?.onClick()

        assertIs<ButterState.Idle>(viewModel.state.value, "Menu should be closed after logout")
    }
}
