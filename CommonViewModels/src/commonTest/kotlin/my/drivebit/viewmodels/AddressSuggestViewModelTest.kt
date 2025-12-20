package my.drivebit.viewmodels

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.test.runTest
import my.drivebit.repositories.AddressSuggestRepository
import my.drivebit.repositories.ResultAddressSuggest
import my.drivebit.repositories.SelectedCityRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private class FakeAddressSuggestRepository : AddressSuggestRepository {
    var shouldReturnError = false
    var errorMessage: String = "Error"
    var lastQuery: String? = null
    var lastCity: String? = null
    var callCount = 0

    override suspend fun suggest(
        query: String,
        city: String,
    ): ResultAddressSuggest {
        lastQuery = query
        lastCity = city
        callCount++
        if (shouldReturnError) {
            return ResultAddressSuggest.Error(errorMessage)
        }
        return ResultAddressSuggest.Success(
            listOf(
                "Москва, ул. Ленина, д. 1",
                "Москва, ул. Пушкина, д. 2",
            ),
        )
    }
}

private class FakeSelectedCityRepository : SelectedCityRepository {
    private var cityId: Int? = null
    private var cityName: String? = "Москва"

    override fun saveCity(
        cityId: Int,
        cityName: String,
    ) {
        this.cityId = cityId
        this.cityName = cityName
    }

    override fun getCityId(): Int? = cityId

    override fun getCityName(): String? = cityName

    override fun clearCity() {
        cityId = null
        cityName = null
    }
}

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class AddressSuggestViewModelTest {
    @Test
    fun `initial state should have empty query and suggestions`() =
        runTest {
            val fakeRepo = FakeAddressSuggestRepository()
            val fakeCityRepo = FakeSelectedCityRepository()
            val viewModel =
                AddressSuggestViewModel(
                    addressSuggestRepository = fakeRepo,
                    selectedCityRepository = fakeCityRepo,
                    coroutineScope = backgroundScope,
                )

            assertEquals("", viewModel.query.value)
            assertTrue(viewModel.suggestions.value.isEmpty())
        }

    @Test
    fun `updateQuery should update query value`() =
        runTest {
            val fakeRepo = FakeAddressSuggestRepository()
            val fakeCityRepo = FakeSelectedCityRepository()
            val viewModel =
                AddressSuggestViewModel(
                    addressSuggestRepository = fakeRepo,
                    selectedCityRepository = fakeCityRepo,
                    coroutineScope = backgroundScope,
                )

            viewModel.updateQuery("Москва")

            assertEquals("Москва", viewModel.query.value)
        }

    @Test
    fun `suggestions should be updated after debounce delay`() =
        runTest {
            val fakeRepo = FakeAddressSuggestRepository()
            val fakeCityRepo = FakeSelectedCityRepository()
            val viewModel =
                AddressSuggestViewModel(
                    addressSuggestRepository = fakeRepo,
                    selectedCityRepository = fakeCityRepo,
                    coroutineScope = backgroundScope,
                )

            viewModel.updateQuery("Москва")
            assertTrue(viewModel.suggestions.value.isEmpty())

            kotlinx.coroutines.delay(3100)

            assertEquals(2, viewModel.suggestions.value.size)
            assertEquals("Москва, ул. Ленина, д. 1", viewModel.suggestions.value[0])
            assertEquals("Москва, ул. Пушкина, д. 2", viewModel.suggestions.value[1])
            assertEquals("Москва", fakeRepo.lastQuery)
        }

    @Test
    fun `should cancel previous request when new query is entered`() =
        runTest {
            val fakeRepo = FakeAddressSuggestRepository()
            val fakeCityRepo = FakeSelectedCityRepository()
            val viewModel =
                AddressSuggestViewModel(
                    addressSuggestRepository = fakeRepo,
                    selectedCityRepository = fakeCityRepo,
                    coroutineScope = backgroundScope,
                )

            viewModel.updateQuery("Москва")
            kotlinx.coroutines.delay(2000)
            viewModel.updateQuery("Санкт-Петербург")
            kotlinx.coroutines.delay(3100)

            assertEquals("Санкт-Петербург", fakeRepo.lastQuery)
        }

    @Test
    fun `should call repository with correct city`() =
        runTest {
            val fakeRepo = FakeAddressSuggestRepository()
            val fakeCityRepo = FakeSelectedCityRepository()
            val viewModel =
                AddressSuggestViewModel(
                    addressSuggestRepository = fakeRepo,
                    selectedCityRepository = fakeCityRepo,
                    coroutineScope = backgroundScope,
                )

            viewModel.updateQuery("Ленина")
            kotlinx.coroutines.delay(3100)

            assertEquals("Ленина", fakeRepo.lastQuery)
            assertEquals("Москва", fakeRepo.lastCity)
            assertEquals(1, fakeRepo.callCount)
        }

    @Test
    fun `should handle error result by keeping empty suggestions`() =
        runTest {
            val fakeRepo =
                FakeAddressSuggestRepository().apply {
                    shouldReturnError = true
                    errorMessage = "Network error"
                }
            val fakeCityRepo = FakeSelectedCityRepository()
            val viewModel =
                AddressSuggestViewModel(
                    addressSuggestRepository = fakeRepo,
                    selectedCityRepository = fakeCityRepo,
                    coroutineScope = backgroundScope,
                )

            viewModel.updateQuery("Москва")
            kotlinx.coroutines.delay(3100)

            assertTrue(viewModel.suggestions.value.isEmpty())
        }

    @Test
    fun `clearSuggestions should clear suggestions list`() =
        runTest {
            val fakeRepo = FakeAddressSuggestRepository()
            val fakeCityRepo = FakeSelectedCityRepository()
            val viewModel =
                AddressSuggestViewModel(
                    addressSuggestRepository = fakeRepo,
                    selectedCityRepository = fakeCityRepo,
                    coroutineScope = backgroundScope,
                )

            viewModel.updateQuery("Москва")
            kotlinx.coroutines.delay(3100)

            assertTrue(viewModel.suggestions.value.isNotEmpty())

            viewModel.clearSuggestions()

            assertTrue(viewModel.suggestions.value.isEmpty())
        }
}
