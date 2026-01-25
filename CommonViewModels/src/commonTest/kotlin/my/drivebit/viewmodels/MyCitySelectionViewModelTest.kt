package my.drivebit.viewmodels

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import my.drivebit.network.services.City
import my.drivebit.repositories.MyCityRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class MockMyCityRepository : MyCityRepository {
    var shouldThrowError = false
    var errorMessage = "Network error"
    var searchResults: Map<String, List<City>> = emptyMap()
    var selectedCityId: Int? = null

    override suspend fun searchCities(query: String): List<City> {
        if (shouldThrowError) {
            throw Exception(errorMessage)
        }
        return searchResults[query] ?: emptyList()
    }

    override val getSelectedCity: kotlinx.coroutines.flow.Flow<City>
        get() {
            val moscowResults = searchResults["Москва"] ?: emptyList()
            val city =
                moscowResults.firstOrNull { it.id == (selectedCityId ?: 158830) }
                    ?: City(id = 158830, name = "Москва")
            return kotlinx.coroutines.flow.flowOf(city)
        }

    override fun selectCity(
        cityId: Int,
        cityName: String,
    ) {
        selectedCityId = cityId
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class MyCitySelectionViewModelTest {
    @Test
    fun `SearchCities intent should set state to Success with cities`() =
        runTest(StandardTestDispatcher()) {
            val testScope = CoroutineScope(SupervisorJob() + this.coroutineContext)
            val mockRepository = MockMyCityRepository()
            mockRepository.searchResults =
                mapOf(
                    "Москва" to
                        listOf(
                            City(id = 1, name = "Москва"),
                            City(id = 2, name = "Санкт-Петербург"),
                        ),
                )
            val viewModel =
                MyCitySelectionViewModelImpl(
                    myCityRepository = mockRepository,
                    coroutineScope = testScope,
                )

            viewModel.handleIntent(MyCitySelectionIntent.SearchCities("Москва"))
            assertIs<MyCitySelectionState.Loading>(viewModel.state.value)
            advanceUntilIdle()
            assertIs<MyCitySelectionState.Success>(viewModel.state.value)
        }

    @Test
    fun `SearchCities intent should return empty list for blank query`() =
        runTest(StandardTestDispatcher()) {
            val testScope = CoroutineScope(SupervisorJob() + this.coroutineContext)
            val mockRepository = MockMyCityRepository()
            val viewModel =
                MyCitySelectionViewModelImpl(
                    myCityRepository = mockRepository,
                    coroutineScope = testScope,
                )

            viewModel.handleIntent(MyCitySelectionIntent.SearchCities(""))
            advanceUntilIdle()

            assertIs<MyCitySelectionState.Success>(viewModel.state.value)
            val successState = viewModel.state.value as MyCitySelectionState.Success
            assertEquals(0, successState.cities.size)
        }

    @Test
    fun `SearchCities intent should set state to Error on exception`() =
        runTest(StandardTestDispatcher()) {
            val testScope = CoroutineScope(SupervisorJob() + this.coroutineContext)
            val mockRepository = MockMyCityRepository()
            mockRepository.shouldThrowError = true
            mockRepository.errorMessage = "Network error"
            val viewModel =
                MyCitySelectionViewModelImpl(
                    myCityRepository = mockRepository,
                    coroutineScope = testScope,
                )

            viewModel.handleIntent(MyCitySelectionIntent.SearchCities("Москва"))
            advanceUntilIdle()

            assertIs<MyCitySelectionState.Error>(viewModel.state.value)
            val errorState = viewModel.state.value as MyCitySelectionState.Error
            assertEquals("Network error", errorState.message)
        }

    @Test
    fun `SearchCities intent should set state to Error with default message on exception with empty message`() =
        runTest(StandardTestDispatcher()) {
            val testScope = CoroutineScope(SupervisorJob() + this.coroutineContext)
            val mockRepository = MockMyCityRepository()
            mockRepository.shouldThrowError = true
            mockRepository.errorMessage = ""
            val viewModel =
                MyCitySelectionViewModelImpl(
                    myCityRepository = mockRepository,
                    coroutineScope = testScope,
                )

            viewModel.handleIntent(MyCitySelectionIntent.SearchCities("Москва"))
            advanceUntilIdle()

            assertIs<MyCitySelectionState.Error>(viewModel.state.value)
            val errorState = viewModel.state.value as MyCitySelectionState.Error
            assertEquals("Не удалось найти города", errorState.message)
        }

    @Test
    fun `SelectCity intent should call repository selectCity`() =
        runTest(StandardTestDispatcher()) {
            val testScope = CoroutineScope(SupervisorJob() + this.coroutineContext)
            val mockRepository = MockMyCityRepository()
            val viewModel =
                MyCitySelectionViewModelImpl(
                    myCityRepository = mockRepository,
                    coroutineScope = testScope,
                )

            viewModel.handleIntent(MyCitySelectionIntent.SelectCity(2, "Санкт-Петербург"))

            assertEquals(2, mockRepository.selectedCityId)
        }

    @Test
    fun `handleIntent SearchCities should search cities`() =
        runTest(StandardTestDispatcher()) {
            val testScope = CoroutineScope(SupervisorJob() + this.coroutineContext)
            val mockRepository = MockMyCityRepository()
            mockRepository.searchResults =
                mapOf(
                    "Москва" to listOf(City(id = 1, name = "Москва")),
                    "Санкт" to
                        listOf(
                            City(id = 1, name = "Москва"),
                            City(id = 2, name = "Санкт-Петербург"),
                        ),
                )
            val viewModel =
                MyCitySelectionViewModelImpl(
                    myCityRepository = mockRepository,
                    coroutineScope = testScope,
                )

            viewModel.handleIntent(MyCitySelectionIntent.SearchCities("Москва"))
            advanceUntilIdle()

            val firstState = viewModel.state.value as MyCitySelectionState.Success
            assertEquals(1, firstState.cities.size)

            viewModel.handleIntent(MyCitySelectionIntent.SearchCities("Санкт"))
            advanceUntilIdle()

            val secondState = viewModel.state.value as MyCitySelectionState.Success
            assertEquals(2, secondState.cities.size)
        }

    @Test
    fun `SearchCities should set Loading state before making request`() =
        runTest(StandardTestDispatcher()) {
            val testScope = CoroutineScope(SupervisorJob() + this.coroutineContext)
            val mockRepository = MockMyCityRepository()
            mockRepository.searchResults =
                mapOf(
                    "Москва" to listOf(City(id = 1, name = "Москва")),
                )
            val viewModel =
                MyCitySelectionViewModelImpl(
                    myCityRepository = mockRepository,
                    coroutineScope = testScope,
                )

            viewModel.handleIntent(MyCitySelectionIntent.SearchCities("Москва"))

            assertIs<MyCitySelectionState.Loading>(viewModel.state.value)

            advanceUntilIdle()

            assertIs<MyCitySelectionState.Success>(viewModel.state.value)
        }
}
