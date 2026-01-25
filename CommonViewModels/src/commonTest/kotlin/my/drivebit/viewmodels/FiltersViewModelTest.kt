package my.drivebit.viewmodels

import my.drivebit.shared.storage.Storage
import kotlin.test.Test
import kotlin.test.assertEquals

class MockStorage : Storage {
    private val storage = mutableMapOf<String, String>()

    override fun isLogined(): Boolean = false

    override fun saveToken(token: String) {
    }

    override fun getToken(): String? = null

    override fun saveRefreshToken(refreshToken: String) {
    }

    override fun getRefreshToken(): String? = null

    override fun logout() {
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
}

class FiltersViewModelTest {
    private val mockStorage = MockStorage()

    @Test
    fun `initial state should have correct selected filter`() {
        val viewModel = FiltersViewModel(mockStorage)
        val initialState = viewModel.state.value

        assertEquals("Все", initialState.selected)
        assertEquals(2, initialState.filters.size)
        assertEquals("Все", initialState.filters[0].title)
        assertEquals("Поблизости", initialState.filters[1].title)
    }

    @Test
    fun `onSelect should update selected filter when selecting All`() {
        val viewModel = FiltersViewModel(mockStorage)

        viewModel.onSelect("Все")

        assertEquals("Все", viewModel.state.value.selected)
    }

    @Test
    fun `onSelect should update selected filter when selecting По близости`() {
        val viewModel = FiltersViewModel(mockStorage)

        viewModel.onSelect("Поблизости")

        assertEquals("Поблизости", viewModel.state.value.selected)
    }

    @Test
    fun `onSelect should handle multiple selections correctly`() {
        val viewModel = FiltersViewModel(mockStorage)

        viewModel.onSelect("Все")
        assertEquals("Все", viewModel.state.value.selected)

        viewModel.onSelect("Поблизости")
        assertEquals("Поблизости", viewModel.state.value.selected)

        viewModel.onSelect("Все")
        assertEquals("Все", viewModel.state.value.selected)
    }

    @Test
    fun `onSelect should maintain filters list unchanged`() {
        val viewModel = FiltersViewModel(mockStorage)
        val initialFilters = viewModel.state.value.filters

        viewModel.onSelect("Поблизости")

        assertEquals(initialFilters.size, viewModel.state.value.filters.size)
        assertEquals(initialFilters, viewModel.state.value.filters)
    }
}
