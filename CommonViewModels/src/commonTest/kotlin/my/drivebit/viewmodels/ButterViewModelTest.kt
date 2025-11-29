package my.drivebit.viewmodels

import my.drivebit.shared.storage.Storage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
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
    fun `initial state should be Idle`() {
        val viewModel = ButterViewModelImpl(mockStorage)

        assertIs<ButterState.Idle>(viewModel.state.value, "Initial state should be Idle")
    }

    @Test
    fun `open should change state to Opened with non-empty list`() {
        val viewModel = ButterViewModelImpl(mockStorage)

        viewModel.onClick()

        assertIs<ButterState.Opened>(viewModel.state.value, "State should be Opened after open()")
        val openedState = viewModel.state.value as ButterState.Opened
        assertFalse(openedState.model.isEmpty(), "Model list should not be empty")
        assertTrue(openedState.model.isNotEmpty(), "Model list should contain at least one element")
    }

    @Test
    fun `opened state should contain beCameAHost item always`() {
        val viewModel = ButterViewModelImpl(mockStorage)

        viewModel.onClick()

        assertIs<ButterState.Opened>(viewModel.state.value)
        val openedState = viewModel.state.value as ButterState.Opened
        val beCameAHostItem = openedState.model.find { it.text == "Сдать авто" }
        assertTrue(beCameAHostItem != null, "State should always contain 'Сдать авто' item")
        assertEquals("images/butter/car-icon.svg", beCameAHostItem.iconUrl)
    }

    @Test
    fun `opened state should contain login and registr when user is not logged in`() {
        mockStorage.setLoggedIn(false)
        val viewModel = ButterViewModelImpl(mockStorage)

        viewModel.onClick()

        assertIs<ButterState.Opened>(viewModel.state.value)
        val openedState = viewModel.state.value as ButterState.Opened
        assertEquals(3, openedState.model.size, "Should contain login, registr, and beCameAHost")
        assertTrue(openedState.model.any { it.text == "Логин" }, "Should contain login")
        assertTrue(openedState.model.any { it.text == "Регистрация" }, "Should contain registr")
    }

    @Test
    fun `opened state should not contain login and registr when user is logged in`() {
        mockStorage.setLoggedIn(true)
        val viewModel = ButterViewModelImpl(mockStorage)

        viewModel.open()

        assertIs<ButterState.Opened>(viewModel.state.value)
        val openedState = viewModel.state.value as ButterState.Opened
        assertEquals(1, openedState.model.size, "Should contain only beCameAHost")
        assertFalse(openedState.model.any { it.text == "Логин" }, "Should not contain login")
        assertFalse(openedState.model.any { it.text == "Регистрация" }, "Should not contain registr")
    }

    @Test
    fun `close should change state from Opened to Idle`() {
        val viewModel = ButterViewModelImpl(mockStorage)

        viewModel.open()
        assertIs<ButterState.Opened>(viewModel.state.value, "State should be Opened after open()")

        viewModel.close()
        assertIs<ButterState.Idle>(viewModel.state.value, "State should be Idle after close()")
    }

    @Test
    fun `onClick should toggle state from Idle to Opened`() {
        val viewModel = ButterViewModelImpl(mockStorage)

        assertIs<ButterState.Idle>(viewModel.state.value, "Initial state should be Idle")

        viewModel.onClick()
        assertIs<ButterState.Opened>(viewModel.state.value, "State should be Opened after first onClick()")
    }

    @Test
    fun `onClick should toggle state from Opened to Idle`() {
        val viewModel = ButterViewModelImpl(mockStorage)

        viewModel.open()
        assertIs<ButterState.Opened>(viewModel.state.value, "State should be Opened after open()")

        viewModel.onClick()
        assertIs<ButterState.Idle>(viewModel.state.value, "State should be Idle after onClick() when Opened")
    }

    @Test
    fun `onClick should toggle state multiple times`() {
        val viewModel = ButterViewModelImpl(mockStorage)

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
}
