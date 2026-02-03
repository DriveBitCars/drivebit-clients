package my.drivebit.viewmodels

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import my.drivebit.network.services.Dictionary
import my.drivebit.network.services.FilterSuggestion
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

class MockDictionary : Dictionary {
    override suspend fun getCarBrands(): List<my.drivebit.network.services.CarBrand> =
        throw NotImplementedError("Not used in FiltersViewModel")

    override suspend fun getCarModels(brandId: Int): List<my.drivebit.network.services.CarModel> =
        throw NotImplementedError("Not used in FiltersViewModel")

    override suspend fun searchCities(query: String): List<my.drivebit.network.services.City> =
        throw NotImplementedError("Not used in FiltersViewModel")

    override suspend fun getAllCities(): List<my.drivebit.network.services.City> =
        throw NotImplementedError("Not used in FiltersViewModel")

    override suspend fun getCarEnums(): my.drivebit.network.services.CarEnumsResponse =
        throw NotImplementedError("Not used in FiltersViewModel")

    override suspend fun getDocumentEnums(): my.drivebit.network.services.DocumentEnumsResponse =
        throw NotImplementedError("Not used in FiltersViewModel")

    override suspend fun getFiltersSuggested(): List<FilterSuggestion> = emptyList()
}

@OptIn(ExperimentalCoroutinesApi::class)
class FiltersViewModelTest {
    private val mockStorage = MockStorage()
    private val mockDictionary = MockDictionary()

    @Test
    fun `initial state should have correct selected filter`() =
        runTest(StandardTestDispatcher()) {
            val viewModel = FiltersViewModel(mockStorage, mockDictionary)
            advanceUntilIdle()
            val initialState = viewModel.state.value

            assertEquals("Все", initialState.selected)
            assertEquals(2, initialState.filters.size)
            assertEquals("Все", initialState.filters[0].title)
            assertEquals("Поблизости", initialState.filters[1].title)
        }

    @Test
    fun `onSelect should update selected filter when selecting All`() =
        runTest(StandardTestDispatcher()) {
            val viewModel = FiltersViewModel(mockStorage, mockDictionary)
            advanceUntilIdle()

            viewModel.onSelect("Все")

            assertEquals("Все", viewModel.state.value.selected)
        }

    @Test
    fun `onSelect should update selected filter when selecting По близости`() =
        runTest(StandardTestDispatcher()) {
            val viewModel = FiltersViewModel(mockStorage, mockDictionary)
            advanceUntilIdle()

            viewModel.onSelect("Поблизости")

            assertEquals("Поблизости", viewModel.state.value.selected)
        }

    @Test
    fun `onSelect should handle multiple selections correctly`() =
        runTest(StandardTestDispatcher()) {
            val viewModel = FiltersViewModel(mockStorage, mockDictionary)
            advanceUntilIdle()

            viewModel.onSelect("Все")
            assertEquals("Все", viewModel.state.value.selected)

            viewModel.onSelect("Поблизости")
            assertEquals("Поблизости", viewModel.state.value.selected)

            viewModel.onSelect("Все")
            assertEquals("Все", viewModel.state.value.selected)
        }

    @Test
    fun `onSelect should maintain filters list unchanged`() =
        runTest(StandardTestDispatcher()) {
            val viewModel = FiltersViewModel(mockStorage, mockDictionary)
            advanceUntilIdle()
            val initialFilters = viewModel.state.value.filters

            viewModel.onSelect("Поблизости")

            assertEquals(initialFilters.size, viewModel.state.value.filters.size)
            assertEquals(initialFilters, viewModel.state.value.filters)
        }
}
