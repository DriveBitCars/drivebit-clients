package my.drivebit.viewmodels

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import my.drivebit.network.services.CarAddress
import my.drivebit.network.services.CarGeneral
import my.drivebit.network.services.CarItem
import my.drivebit.network.services.CarSearchResponse
import my.drivebit.repositories.CarSearchRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class MockSearchCarSearchRepository : CarSearchRepository {
    var shouldThrowError = false
    var errorMessage = "Network error"
    var searchResult: CarSearchResponse =
        CarSearchResponse(
            cars =
                listOf(
                    testCarItem(
                        id = "car-1",
                        brandName = "BMW",
                        modelName = "X5",
                        year = 2021,
                    ),
                ),
        )

    override val searchCarsByUserCity: Flow<CarSearchResponse>
        get() {
            if (shouldThrowError) {
                return kotlinx.coroutines.flow.flow {
                    throw Exception(errorMessage)
                }
            }
            return kotlinx.coroutines.flow.flowOf(searchResult)
        }
}

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {
    private fun createViewModel(
        repository: MockSearchCarSearchRepository,
        dispatcher: CoroutineDispatcher,
    ): SearchViewModelImpl =
        SearchViewModelImpl(
            carSearchRepository = repository,
            coroutineScope = CoroutineScope(SupervisorJob() + dispatcher),
        )

    @Test
    fun `initial state should emit SearchResults when repository emits data`() =
        runTest {
            val dispatcher = StandardTestDispatcher(testScheduler)
            val repository = MockSearchCarSearchRepository()
            val viewModel = createViewModel(repository = repository, dispatcher = dispatcher)

            assertIs<SearchState.Loading>(viewModel.state.value)

            advanceUntilIdle()

            assertIs<SearchState.SearchResults>(viewModel.state.value)
            val resultsState = viewModel.state.value as SearchState.SearchResults
            assertEquals(1, resultsState.cars.size)
            assertEquals("BMW", resultsState.cars[0].general.brandName)
        }

    @Test
    fun `loadSearchResults should set Error when repository fails`() =
        runTest {
            val dispatcher = StandardTestDispatcher(testScheduler)
            val repository = MockSearchCarSearchRepository()
            repository.shouldThrowError = true
            repository.errorMessage = "Search failed"
            val viewModel = createViewModel(repository = repository, dispatcher = dispatcher)

            advanceUntilIdle()

            assertIs<SearchState.Error>(viewModel.state.value)
            val errorState = viewModel.state.value as SearchState.Error
            assertEquals("Search failed", errorState.message)
        }

    @Test
    fun `loadSearchResults can be called again to refresh results`() =
        runTest {
            val dispatcher = StandardTestDispatcher(testScheduler)
            val repository = MockSearchCarSearchRepository()
            val viewModel = createViewModel(repository = repository, dispatcher = dispatcher)

            advanceUntilIdle()

            repository.searchResult = CarSearchResponse(emptyList())
            viewModel.loadSearchResults()

            advanceUntilIdle()

            assertIs<SearchState.SearchResults>(viewModel.state.value)
            val refreshedState = viewModel.state.value as SearchState.SearchResults
            assertEquals(0, refreshedState.cars.size)
        }
}

private fun testCarItem(
    id: String,
    brandName: String,
    modelName: String,
    year: Int? = null,
): CarItem =
    CarItem(
        id = id,
        year = year,
        general =
            CarGeneral(
                brandName = brandName,
                modelName = modelName,
                vin = "VIN123",
                seats = 4,
                address =
                    CarAddress(
                        geoLat = 0.0,
                        geoLon = 0.0,
                    ),
            ),
    )
